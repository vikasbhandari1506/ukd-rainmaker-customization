package org.egov.ukdcustomservice.service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.egov.ukdcustomservice.enums.Channel;
import org.egov.ukdcustomservice.models.Assessment;
import org.egov.ukdcustomservice.models.Assessment.Source;
import org.egov.ukdcustomservice.models.Demand;
import org.egov.ukdcustomservice.models.DemandDetail;
import org.egov.ukdcustomservice.models.Property;
import org.egov.ukdcustomservice.models.RollOverCount;
import org.egov.ukdcustomservice.producer.Producer;
import org.egov.ukdcustomservice.repository.RollOverCountRowMapper;
import org.egov.ukdcustomservice.repository.RollOverRepository;
import org.egov.ukdcustomservice.util.RollOverUtil;
import org.egov.ukdcustomservice.web.contract.AssessmentRequest;
import org.egov.ukdcustomservice.web.contract.AssessmentResponse;
import org.egov.ukdcustomservice.web.contract.DemandResponse;
import org.egov.ukdcustomservice.web.contract.DemandSearchCriteria;
import org.egov.ukdcustomservice.web.contract.PropertyCriteria;
import org.egov.ukdcustomservice.web.contract.PropertyResponse;
import org.egov.ukdcustomservice.web.contract.PropertyRollOverCountRequest;
import org.egov.ukdcustomservice.web.contract.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RollOverService {
	
	@Autowired
    private JdbcTemplate jdbcTemplate;

	@Autowired
    private RollOverRepository rollOverRepository;
	
	@Autowired
    private ObjectMapper mapper;
	
	@Autowired
    private RollOverCountRowMapper rollOverCountRowMapper;

	@Autowired
    private Producer producer;

	@Autowired
    private RollOverUtil util; 
	
	
	@Value("${egov.property-services.host}")
    private String ptHost;

    @Value("${egov.property.plainsearch}")
    private String propertySearchEndpoint;

    @Value("${egov.property.assessment.create}")
    private String assessmentCreateEndpoint;

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndpoint;
   
    @Value("${rollover.batch.value}")
    private Integer batchSize;

    @Value("${rollover.offset.value}")
    private Integer batchOffset;
    
    @Value("${persister.rollover.batch.count.topic}")
    private String rollOverBatchCountTopic;

    @Value("${rollover.current.finyear}")
    private String rollOverCurrFinYear;

    @Value("${rollover.next.finyear}")
    private String rollOverNextFinYear;

    private Integer count2=0;

	
    public static final String COUNT_QUERY = "select count(*) from eg_pt_property where tenantid = '{}';";
    public static final String TENANT_QUERY = "select distinct tenantid from eg_pt_property;";
    public static final String MIGARTION_COUNT_QUERY = "select count(*) from eg_pt_rolloverbatch;";
    public static final String MIGARTION_POINT_QUERY ="select id,batch,batchsize,createdtime,tenantid,recordCount from eg_pt_rolloverbatch as rollover where tenantid = ? and createdtime = (select max(createdtime) from eg_pt_rolloverbatch where tenantid = ?);";

    
    public static final String URL_PARAMS_SEPARATER = "?";
    
    public static final String TENANT_ID_FIELD_FOR_SEARCH_URL = "tenantId=";

    public static final String LIMIT_FIELD_FOR_SEARCH_URL = "limit=";

    public static final String LOCALITY_FIELD_FOR_SEARCH_URL = "locality=";

    public static final String OFFSET_FIELD_FOR_SEARCH_URL = "offset=";

    public static final String SEPARATER = "&";
    
    public static final String INVALID_TENANT_ID_MDMS_KEY = "INVALID TENANTID";
    public static final String INVALID_TENANT_ID_MDMS_MSG = "No data found for this tenantID";
    public static final String CURR_FinYear = "2020-21";
    public static final String NEXT_FinYear = "2021-22";
    		
    public Map<String, String> initiateProcess(RequestInfoWrapper requestInfoWrapper,PropertyCriteria propertyCriteria,Map<String, String> errorMap, List<String> tenantIdList){

        Map<String, String> resultMap = null;
        List<Map<String, Object>> masters = getMasterFinancialYearData(propertyCriteria.getTenantId(), requestInfoWrapper.getRequestInfo());
        
		List<String> tenantList = null;

		if (!CollectionUtils.isEmpty(tenantIdList)) {
			tenantList = tenantIdList;
		} 
//		else
//			tenantList = getTenantList();
        if(StringUtils.isEmpty(propertyCriteria.getLimit()))
            propertyCriteria.setLimit(Long.valueOf(batchSize));

        if(StringUtils.isEmpty(propertyCriteria.getOffset()))
            propertyCriteria.setOffset(Long.valueOf(batchOffset));

        for(int i= 0;i<tenantList.size();i++){
        	RollOverCount rollOverCount = getRollOverCountForTenant(tenantList.get(i));
        	log.info("\n\nMigration count--->"+rollOverCount.toString()+"\n\n");
            if(ObjectUtils.isEmpty(rollOverCount) || rollOverCount.getId() == null){
                propertyCriteria.setTenantId(tenantList.get(i));
                resultMap = initiateRollOver(requestInfoWrapper, propertyCriteria,masters,errorMap);
            }
            else{
                long count = getTenantCount(tenantList.get(i));

                log.info("\n\ntenant--->"+tenantList.get(i)+"\n\n");//FIXME: Remove System out println
                log.info("\n\ncount--->"+count+"\n\n");

                if(rollOverCount.getRecordCount() >= count){
                	propertyCriteria.setTenantId(tenantList.get(i));
                    resultMap = rollOverForFailedProps(requestInfoWrapper,propertyCriteria,masters,errorMap);
                }else{
                    propertyCriteria.setTenantId(tenantList.get(i));
                    propertyCriteria.setOffset(rollOverCount.getOffset()+rollOverCount.getLimit());
                    resultMap = initiateRollOver(requestInfoWrapper, propertyCriteria,masters,errorMap);
                }

            }
        }
        return resultMap;
    }

	private Map<String, String> rollOverForFailedProps(RequestInfoWrapper requestInfoWrapper,
			PropertyCriteria propertyCriteria, List<Map<String, Object>> masters, Map<String, String> errorMap) {

		List<Map<String, Object>> failedProps = rollOverRepository.fetchPropertiesForRollOver(propertyCriteria.getTenantId());
		log.info("Count Props:"+failedProps.size());
		RequestInfo requestInfo =  requestInfoWrapper.getRequestInfo();
        Map<String, String> responseMap = new HashMap<>();
		List<Map<String, Object>> currentFinYear = masters.stream().filter(master -> master.get("code").equals("2021-22")).collect(Collectors.toList());
		List<Map<String, Object>> previousFinYear = masters.stream().filter(master -> master.get("code").equals(CURR_FinYear)).collect(Collectors.toList());

		for (Map<String, Object> prop : failedProps) {
			DemandSearchCriteria criteria = new DemandSearchCriteria();
			criteria.setTenantId(prop.get("tenantid").toString());
			criteria.setPropertyId(prop.get("propertyid").toString());

			List<Demand> demands = new ArrayList<Demand>();
			DemandResponse res = mapper.convertValue((rollOverRepository.fetchResult(util.getDemandSearchUrl(criteria),
					new RequestInfoWrapper(requestInfo))).get(), DemandResponse.class);
			if (res.getDemands().size() == 0) {
				rollOverRepository.saveRollOver(prop.get("propertyid").toString(), prop.get("tenantid").toString(),
						"2021-22", "NOTINITIATED", "No Demands found this property ");
			} else {
				demands.addAll(res.getDemands());
				List<Demand> currDemand = demands.stream()
						.filter(dmnd -> dmnd.getTaxPeriodFrom()
								.equals(Long.valueOf(currentFinYear.get(0).get("startingDate").toString())))
						.collect(Collectors.toList());
				if(CollectionUtils.isEmpty(currDemand)){
					rollOverRepository.saveRollOver(prop.get("propertyid").toString(), prop.get("tenantid").toString(),
							"2021-22", "NOTINITIATED", "No Demands found this property ");
				}
				else if (currDemand.get(0) != null) {
					rollOverRepository.saveRollOver(prop.get("propertyid").toString(), prop.get("tenantid").toString(),
							"2021-22", "SUCCESS", "Roll Over is Successfully Done");
					responseMap.put(prop.get("propertyid").toString(), "Success");
				} else {
					List<Demand> prevDemand = demands.stream()
							.filter(dmnd -> dmnd.getTaxPeriodFrom()
									.equals(Long.valueOf(previousFinYear.get(0).get("startingDate").toString())))
							.collect(Collectors.toList());
					if(CollectionUtils.isEmpty(prevDemand)){
						rollOverRepository.saveRollOver(prop.get("propertyid").toString(), prop.get("tenantid").toString(),
								"2021-22", "NOTINITIATED", "No Demands found this property ");
					}
					else if (prevDemand.get(0) != null) {
						List<Demand> newDemands = prepareDemandRequest(prevDemand.get(0), masters);
						try {

							createAssessmentForRollOver(newDemands, requestInfo, prop.get("tenantid").toString(),
									prop.get("propertyid").toString());

							rollOverRepository.saveRollOver(prop.get("propertyid").toString(),
									prop.get("tenantid").toString(), "2021-22", "SUCCESS",
									"Roll Over is Successfully Done");
							responseMap.put(prop.get("propertyid").toString(), "Success");
						} catch (Exception e) {
							log.error("Assessment Creation Failed " + e.toString());
							rollOverRepository.saveRollOver(prop.get("propertyid").toString(),
									prop.get("tenantid").toString(), "2021-22", "FAILED",
									"Assessment or Demand Creation Failed");
							responseMap.put(prop.get("propertyid").toString(), "Failure");
						}
					} else {
						rollOverRepository.saveRollOver(prop.get("propertyid").toString(),
								prop.get("tenantid").toString(), "2021-22", "NOTINITIATED",
								"No Demands found this property ");
					}
				}
			}
		}

		return responseMap;
	}

	public Map<String, String> initiateRollOver(RequestInfoWrapper requestInfoWrapper,PropertyCriteria propertyCriteria,List<Map<String, Object>> masters,Map<String, String> errorMap) {

		 RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();
	        List<Property> properties = new ArrayList<>();
	        Map<String, String> responseMap = new HashMap<>();

	        Integer startBatch = Math.toIntExact(propertyCriteria.getOffset());
	        Integer batchSizeInput = Math.toIntExact(propertyCriteria.getLimit());


	        long count = getTenantCount(propertyCriteria.getTenantId());
	        log.info("Count: "+count);
	        log.info("startbatch: "+startBatch);

	            while(startBatch<count) {
	                long startTime = System.nanoTime();
	                properties = searchPropertyFromURL(requestInfoWrapper,propertyCriteria) ;
	                try {
	                    properties= rollOverProperty(requestInfo,properties,masters,errorMap);
	                } catch (Exception e) {

	                    log.error("Migration failed at batch count of : " + startBatch);
	                    responseMap.put( "Migration failed at batch count : " + startBatch, e.getMessage());
	                    return responseMap;
	                }
	                addResponseToMap(properties,responseMap,"SUCCESS");
	                log.info(" count completed for batch : " + startBatch);
	                long endtime = System.nanoTime();
	                long elapsetime = endtime - startTime;
	                log.info("\n\nBatch elapsed time: "+elapsetime+"\n\n");

	                RollOverCount rollOverCount = new RollOverCount();
	                rollOverCount.setId(UUID.randomUUID().toString());
	                rollOverCount.setOffset(Long.valueOf(startBatch));
	                rollOverCount.setLimit(Long.valueOf(batchSizeInput));
	                rollOverCount.setCreatedTime(System.currentTimeMillis());
	                rollOverCount.setTenantid(propertyCriteria.getTenantId());
	                rollOverCount.setRecordCount(Long.valueOf(startBatch+batchSizeInput));
	                PropertyRollOverCountRequest request = PropertyRollOverCountRequest.builder().requestInfo(requestInfo).rollOverCount(rollOverCount).build();
	                producer.push(rollOverBatchCountTopic, request);

	                startBatch = startBatch+batchSizeInput;
	                propertyCriteria.setOffset(Long.valueOf(startBatch));
	                System.out.println("Property Count which pushed into kafka topic:"+count2);
	            }
	        propertyCriteria.setOffset(Long.valueOf(batchOffset));
	        return responseMap;

			}

	private List<Property> rollOverProperty(RequestInfo requestInfo, List<Property> properties,
			List<Map<String, Object>> masters, Map<String, String> errorMap) {

		List<Map<String, Object>> previousFinYear = masters.stream().filter(master -> master.get("code").equals(CURR_FinYear)).collect(Collectors.toList());

		if (!CollectionUtils.isEmpty(properties)) {
			for (Property property : properties) {
				DemandSearchCriteria criteria = new DemandSearchCriteria();
				criteria.setTenantId(property.getTenantId());
				criteria.setPropertyId(property.getPropertyId());
				criteria.setPeriodFrom(Long.valueOf(previousFinYear.get(0).get("startingDate").toString()));

				List<Demand> demands = new ArrayList<Demand>();
				DemandResponse res = mapper.convertValue(( rollOverRepository
						.fetchResult(util.getDemandSearchUrl(criteria), new RequestInfoWrapper(requestInfo))).get(),
						DemandResponse.class);

				if (!CollectionUtils.isEmpty(res.getDemands())) {
					demands.addAll(res.getDemands());

					if((res.getDemands()).size() < 2){
					Demand demand = demands.get(0);
					List<Demand> newDemands = prepareDemandRequest(demand, masters);
					try{
						createAssessmentForRollOver(newDemands, requestInfo, property.getTenantId(), property.getPropertyId());
					
						rollOverRepository.saveRollOver(property.getPropertyId(), property.getTenantId(), "2021-22", "SUCCESS",
								"Roll Over is Successfully Done");
					}
					catch(Exception e)
					{						
						log.error("Assessment Creation Failed "+e.toString());
						rollOverRepository.saveRollOver(property.getPropertyId(), property.getTenantId(), "2021-22", "FAILED",
								"Assessment or Demand Creation Failed");
					}
					}else{
						rollOverRepository.saveRollOver(property.getPropertyId(), property.getTenantId(), "2021-22", "SUCCESS",
								"Roll Over is Successfully Done");
					}
					} else {
					rollOverRepository.saveRollOver(property.getPropertyId(), property.getTenantId(), "2021-22", "NOTINITIATED",
							"No Demand found for the Current Financial Year");
				}
			}
		} else {
			throw new CustomException("EG_PT_PROPERTY_ROLLOVER_ERROR",
					"No Properties found for roll over in given criteria");
		}
		return properties;

	}

	private List<Map<String, Object>> getMasterFinancialYearData(String tenantid, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder(mdmsHost).append(mdmsEndpoint);
        List<MasterDetail> masterDetails = new ArrayList<>();
        masterDetails.add(MasterDetail.builder().name("FinancialYear").build());
        List<ModuleDetail> moduleDetails = new ArrayList<>();
        moduleDetails.add(ModuleDetail.builder().moduleName("egf-master").masterDetails(masterDetails).build());
        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId("uk").moduleDetails(moduleDetails).build();
        MdmsCriteriaReq req = MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
        
        try {
			Optional<Object> result =  rollOverRepository.fetchResult(uri, req);
            return JsonPath.read(result.get(),"$.MdmsRes.egf-master.FinancialYear");
        } catch (Exception e) {
            throw new CustomException(INVALID_TENANT_ID_MDMS_KEY,
                    INVALID_TENANT_ID_MDMS_MSG);
        }
		
	}

	private Assessment createAssessmentForRollOver(List<Demand> newDemands, RequestInfo requestInfo, String tenantId,String propertyId) {
		Assessment assessment = Assessment.builder().assessmentDate(ZonedDateTime.now().toInstant().toEpochMilli()).financialYear("2021-22").channel(Channel.CFC_COUNTER).source(Source.LEGACY_RECORD).tenantId(tenantId).propertyId(propertyId).build();
		ObjectNode additionalDetails = new ObjectMapper().createObjectNode();
		assessment.setAdditionalDetails(additionalDetails.set("Demands", new ObjectMapper().valueToTree(newDemands)));
		assessment.setAdditionalDetails(additionalDetails.set("RequestInfo", new ObjectMapper().valueToTree(requestInfo)));
		assessment.setAdditionalDetails(additionalDetails.set("isRollOver", new ObjectMapper().valueToTree(true)));
		AssessmentRequest request = AssessmentRequest.builder().requestInfo(requestInfo).assessment(assessment).build();
		
		StringBuilder url = new StringBuilder(ptHost).append(assessmentCreateEndpoint);
		AssessmentResponse res = mapper.convertValue(rollOverRepository.fetchServiceResult(url, request), AssessmentResponse.class);
		
		return res.getAssessments().get(0);
	}


	private List<Demand> prepareDemandRequest(Demand demand, List<Map<String, Object>> finYears) {
		List<Demand> newDemands = new ArrayList<Demand>();
		List<DemandDetail> details = demand.getDemandDetails().stream().filter(dmnddtls -> dmnddtls.getTaxHeadMasterCode().equals("PT_TAX") || dmnddtls.getTaxHeadMasterCode().equals("SWATCHATHA_TAX")).collect(Collectors.toList());
		List<DemandDetail> newDetails = new ArrayList<DemandDetail>();
		Map<String, Object> financialYear = finYears.stream().filter(finYear -> finYear.get("code").equals(NEXT_FinYear)).collect(Collectors.toList()).get(0);		
		details.forEach( detail -> {
			DemandDetail newDetail = new DemandDetail();
			newDetail.setTaxHeadMasterCode(detail.getTaxHeadMasterCode());
			newDetail.setTaxAmount(detail.getTaxAmount());
			newDetail.setCollectionAmount(BigDecimal.ZERO);
			newDetail.setTenantId(demand.getTenantId());
			
			newDetails.add(newDetail);
		});
		demand.setDemandDetails(newDetails);
		demand.setId(null);
		demand.setTaxPeriodFrom(Long.valueOf(financialYear.get("startingDate").toString()));
		demand.setTaxPeriodTo(Long.valueOf(financialYear.get("endingDate").toString()));
		newDemands.add(demand);
		return newDemands;		
	}

	public long getTenantCount(String tenantid) {
		String query = COUNT_QUERY.replace("{}", tenantid);
		long count = (long) jdbcTemplate.queryForObject(query, Integer.class);
		return count;
	}

	public List<String> getTenantList() {
		List<String> tenantList = jdbcTemplate.queryForList(TENANT_QUERY, String.class);
		return tenantList;
	}

	public RollOverCount getRollOverCountForTenant(String tenantId) {
		RollOverCount rollOverCount = jdbcTemplate.query(MIGARTION_POINT_QUERY, new Object[] { tenantId, tenantId },
				rollOverCountRowMapper);
		return rollOverCount;
	}
	

    public List<Property> searchPropertyFromURL(RequestInfoWrapper requestInfoWrapper,PropertyCriteria propertyCriteria){


        StringBuilder url = new StringBuilder(ptHost).append(propertySearchEndpoint).append(URL_PARAMS_SEPARATER)
                .append(TENANT_ID_FIELD_FOR_SEARCH_URL).append(propertyCriteria.getTenantId())
                .append(SEPARATER).append(OFFSET_FIELD_FOR_SEARCH_URL).append(propertyCriteria.getOffset())
                .append(SEPARATER).append(LIMIT_FIELD_FOR_SEARCH_URL).append(propertyCriteria.getLimit());

        if(propertyCriteria.getLocality() != null){
        	url = url.append(SEPARATER).append(LIMIT_FIELD_FOR_SEARCH_URL).append(propertyCriteria.getLocality());
        }
        
        PropertyResponse res = mapper.convertValue(rollOverRepository.fetchResult(url, requestInfoWrapper).get(), PropertyResponse.class);


        return res.getProperties();
    }
    
    private void addResponseToMap(List<Property> properties, Map<String, String> responseMap, String message) {

        properties.forEach(property -> {

            responseMap.put(property.getPropertyId(), message);
            log.info("The property id : " + property.getPropertyId() + " message : " + message);
        });
    }
}
