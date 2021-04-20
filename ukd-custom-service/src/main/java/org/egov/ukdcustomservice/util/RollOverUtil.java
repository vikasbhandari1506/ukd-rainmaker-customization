package org.egov.ukdcustomservice.util;

import org.egov.ukdcustomservice.web.contract.DemandSearchCriteria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RollOverUtil {
	
	@Value("${egbs.host}")
    private String egbsHost;
    
    @Value("${egbs.demand.search.endpoint}")
	private String egbsSearchDemand;
    

	public static final String URL_PARAMS_SEPARATER = "?";

    public static final String TENANT_ID_FIELD_FOR_SEARCH_URL = "tenantId=";

    public static final String SEPARATER = "&";

	public StringBuilder getDemandSearchUrl(DemandSearchCriteria criteria) {

		StringBuilder url = new StringBuilder().append(egbsHost).append(egbsSearchDemand);

		if (criteria.getTenantId() != null) {
			url.append(URL_PARAMS_SEPARATER).append(TENANT_ID_FIELD_FOR_SEARCH_URL).append(criteria.getTenantId());
		}
		if (criteria.getPropertyId() != null) {
			url.append(SEPARATER).append("consumerCode=").append(criteria.getPropertyId());
		}
		if (criteria.getPeriodFrom() != null) {
			url.append(SEPARATER).append("periodFrom=").append(criteria.getPeriodFrom());
		}
		url.append(SEPARATER).append("status=").append("ACTIVE");

		return url;
    }
}
