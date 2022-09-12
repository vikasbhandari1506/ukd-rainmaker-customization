package org.egov.ukdcustomservice.service;

import org.apache.commons.lang3.StringUtils;
import org.egov.tracer.model.CustomException;
import org.javers.common.collections.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DisableModulesService {

	
    @Autowired
    private ObjectMapper mapper;
	
    @Value("${egov.restrict.access.endpoints}")
    private String restrictedEndPoints;
    
    @Value("${egov.pt.disable.tenants}")
    private String ptRestrictedTenants;
    
	public void restrictApis(String tenantId, String actionURL, Object customRequest) {
		try {
			log.info("customRequest {}", customRequest);
			log.info(mapper.writeValueAsString(customRequest));
		} catch (JsonProcessingException e) {
			log.error("Error occurred on parsing::: {}", e.getMessage());
		}
		String[] restrictEndPoints= restrictedEndPoints.split(",");
		String[] ptRestenants = ptRestrictedTenants.split(",");
		
		if (StringUtils.isNoneBlank(actionURL) && Arrays.asList(restrictEndPoints).contains(actionURL)
				&& StringUtils.isNoneBlank(tenantId) && Arrays.asList(ptRestenants).contains(tenantId)) {
			log.info("Access is restricted");
			throw new CustomException("NO_ACCESS", "Access for "+actionURL+" is restricted for the "+tenantId);
		}
	}
}
