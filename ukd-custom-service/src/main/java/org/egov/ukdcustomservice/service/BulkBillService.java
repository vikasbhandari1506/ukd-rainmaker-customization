package org.egov.ukdcustomservice.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ukdcustomservice.models.GenerateBillCriteria;
import org.egov.ukdcustomservice.models.PropertyCount;
import org.egov.ukdcustomservice.models.RollOverCount;
import org.egov.ukdcustomservice.producer.Producer;
import org.egov.ukdcustomservice.repository.PropertyCountRowMapper;
import org.egov.ukdcustomservice.repository.RollOverRepository;
import org.egov.ukdcustomservice.web.contract.BillResponseV2;
import org.egov.ukdcustomservice.web.contract.PropertyRollOverCountRequest;
import org.egov.ukdcustomservice.web.contract.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BulkBillService {

	@Autowired
	private RollOverRepository repository;

	@Autowired
	private RollOverService rollOverService;

	@Autowired
	private Producer producer;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PropertyCountRowMapper propertyCountRowMapper;

	@Autowired
	private ObjectMapper mapper;

	@Value("${egbs.host}")
	private String billHost;

	@Value("${egbs.fetchbill.endpoint}")
	private String fetchBillEndPoint;

	@Value("${rollover.batch.value}")
	private Integer batchSize;

	@Value("${rollover.offset.value}")
	private Integer batchOffset;

	@Value("${persister.bulkbill.batch.count.topic}")
	private String bulkBillBatchCountTopic;

	public static final String URL_PARAMS_SEPARATER = "?";

	public static final String SEPARATER = "&";
	public static final String MIGARTION_POINT_QUERY = "select id,batch,batchsize,createdtime,tenantid,recordCount from eg_pt_rolloverbatch as rollover where tenantid = ? and createdtime = (select max(createdtime) from eg_pt_rolloverbatch where tenantid = ?);";

	public Map<String, String> generateBulkBills(RequestInfoWrapper requestInfoWrapper, GenerateBillCriteria criteria) {
		Map<String, String> resultMap = null;

		if (StringUtils.isEmpty(criteria.getLimit()))
			criteria.setLimit(Long.valueOf(batchSize));

		if (StringUtils.isEmpty(criteria.getOffset()))
			criteria.setOffset(Long.valueOf(batchOffset));

		PropertyCount propertyCount = getPropertyCountForTenant(criteria.getTenantId());
		log.info("\n\nProperty count--->" + propertyCount.toString() + "\n\n");
		if (ObjectUtils.isEmpty(propertyCount) || propertyCount.getId() == null) {
			resultMap = initiategeneration(requestInfoWrapper, criteria);

		} else {
			long count = rollOverService.getTenantCount(criteria.getTenantId());

			System.out.println("\n\ntenant--->" + criteria.getTenantId() + "\n\n");
			System.out.println("\n\ncount--->" + count + "\n\n");

			if (propertyCount.getRecordCount() < count) {
				criteria.setTenantId(criteria.getTenantId());
				criteria.setOffset(propertyCount.getOffset() + propertyCount.getLimit());
				resultMap = initiategeneration(requestInfoWrapper, criteria);
			}
		}

		return resultMap;

	}

	public Map<String, String> initiategeneration(RequestInfoWrapper requestInfoWrapper,
			GenerateBillCriteria criteria) {
		RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();
		List<Map<String, Object>> propertyIds = null;
		Map<String, String> responseMap = new HashMap<>();
		long count = rollOverService.getTenantCount(criteria.getTenantId());

		Integer startBatch = Math.toIntExact(criteria.getOffset());
		Integer batchSizeInput = Math.toIntExact(criteria.getLimit());

		log.info("Count: " + count);
		log.info("startbatch: " + criteria.getOffset());

		while (startBatch < count) {
			long startTime = System.nanoTime();
			propertyIds = repository.fetchPropertiyIds(criteria);
			try {
				for (Map<String, Object> id : propertyIds) {
					StringBuilder url = new StringBuilder(billHost).append(fetchBillEndPoint)
							.append(URL_PARAMS_SEPARATER).append("tenantId=").append(criteria.getTenantId())
							.append(SEPARATER).append("businessService=").append(criteria.getBusinessService())
							.append(SEPARATER).append("consumerCode=").append(id.get("propertyid").toString());
					try {
						mapper.convertValue(repository.fetchResult(url, requestInfoWrapper).get(),
								BillResponseV2.class);
					} catch (Exception e) {
						log.error(e.toString());
					}
				}
			} catch (Exception e) {

				log.error("Migration failed at batch count of : " + startBatch);
				responseMap.put("Migration failed at batch count : " + startBatch, e.getMessage());
				return responseMap;
			}
			addResponseToMap(propertyIds, responseMap, "SUCCESS");
			log.info(" count completed for batch : " + startBatch);
			long endtime = System.nanoTime();
			long elapsetime = endtime - startTime;
			log.info("\n\nBatch elapsed time: " + elapsetime + "\n\n");

			RollOverCount rollOverCount = new RollOverCount();
			rollOverCount.setId(UUID.randomUUID().toString());
			rollOverCount.setOffset(Long.valueOf(startBatch));
			rollOverCount.setLimit(Long.valueOf(batchSizeInput));
			rollOverCount.setCreatedTime(System.currentTimeMillis());
			rollOverCount.setTenantid(criteria.getTenantId());
			rollOverCount.setRecordCount(Long.valueOf(startBatch + batchSizeInput));
			PropertyRollOverCountRequest request = PropertyRollOverCountRequest.builder().requestInfo(requestInfo)
					.rollOverCount(rollOverCount).build();
			producer.push(bulkBillBatchCountTopic, request);

			startBatch = startBatch + batchSizeInput;
			criteria.setOffset(Long.valueOf(startBatch));
		}
		criteria.setOffset(Long.valueOf(batchOffset));
		return responseMap;
	}

	private PropertyCount getPropertyCountForTenant(String tenantId) {
		PropertyCount propertyCount = jdbcTemplate.query(MIGARTION_POINT_QUERY, new Object[] { tenantId, tenantId },
				propertyCountRowMapper);
		return propertyCount;
	}

	private void addResponseToMap(List<Map<String, Object>> propertyIds, Map<String, String> responseMap,
			String message) {

		propertyIds.forEach(property -> {

			responseMap.put(property.get("propertyid").toString(), message);
			log.info("The property id : " + property.get("propertyid").toString() + " message : " + message);
		});

	}
}