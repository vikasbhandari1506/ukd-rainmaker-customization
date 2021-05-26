package org.egov.ukdcustomservice.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.egov.tracer.model.ServiceCallException;
import org.egov.ukdcustomservice.models.GenerateBillCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class RollOverRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RestTemplate restTemplate;


    public void saveRollOver(String propertyId, String tenantId, String finYear, String status, String reason) {
		List<Object> preparedStmtList = new ArrayList<>();
		String basequery = "insert into eg_pt_rollover (propertyid, tenantid, financialyear, status, reason) values (?, ?, ?, ?, ?) ON CONFLICT (propertyid) DO UPDATE SET financialyear=EXCLUDED.financialyear, status=EXCLUDED.status, reason=EXCLUDED.reason";
		StringBuilder builder = new StringBuilder(basequery);
		preparedStmtList.add(propertyId);
		preparedStmtList.add(tenantId);
		preparedStmtList.add(finYear);
		preparedStmtList.add(status);
		preparedStmtList.add(reason);
		jdbcTemplate.update(builder.toString(), preparedStmtList.toArray());
	
	}

	public List<Map<String, Object>> fetchPropertiesForRollOver(String tenantid) {
		List<Object> preparedStmtList = new ArrayList<>();
		String basequery = "select * from  eg_pt_rollover ";
		StringBuilder builder = new StringBuilder(basequery);
		builder.append("where tenantid = ?");
		builder.append(" and (status = 'FAILED' OR status = 'NOTINITIATED')");
		preparedStmtList.add(tenantid);
		log.info("Query : "+builder.toString());
		return jdbcTemplate.queryForList(builder.toString(), preparedStmtList.toArray());
	}
	
	public List<Map<String, Object>> fetchPropertiyIds(GenerateBillCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String basequery = "select propertyid from  eg_pt_property ";
		StringBuilder builder = new StringBuilder(basequery);
		builder.append("where tenantid = ? ");
		preparedStmtList.add(criteria.getTenantId());
		builder.append("OFFSET ? ");
		preparedStmtList.add(criteria.getOffset());
		builder.append("LIMIT ? ");
		preparedStmtList.add(criteria.getLimit());
		log.info("Query : "+builder.toString());
		return jdbcTemplate.queryForList(builder.toString(), preparedStmtList.toArray());
	}
	 public Optional<Object> fetchResult(StringBuilder uri, Object request) {
	        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	        Object response = null;
	        log.info("URI: "+uri.toString());
	        try {
	            log.info("Request: "+mapper.writeValueAsString(request));
	            response = restTemplate.postForObject(uri.toString(), request, Map.class);
	        }catch(HttpClientErrorException e) {
	            log.error("External Service threw an Exception: ",e);
	            throw new ServiceCallException(e.getResponseBodyAsString());
	        }catch(Exception e) {
	            log.error("Exception while fetching from external service: ",e);
	        }

	        return Optional.ofNullable(response);
	    }

	 public Object fetchServiceResult(StringBuilder uri, Object request) {
	        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	        Object response = null;
	        log.info("URI: "+uri.toString());
	        try {
	            log.info("Request: "+mapper.writeValueAsString(request));
	            response = restTemplate.postForObject(uri.toString(), request, Map.class);
	        }catch(HttpClientErrorException e) {
	            log.error("External Service threw an Exception: ",e);
	            throw new ServiceCallException(e.getResponseBodyAsString());
	        }catch(Exception e) {
	            log.error("Exception while fetching from external service: ",e);
	        }

	        return response;
	    }
}