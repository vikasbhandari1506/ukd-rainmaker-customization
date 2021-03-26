package org.egov.ukdcustomservice.web.contract;


import lombok.Data;

@Data
public class DemandSearchCriteria {

    private String tenantId;

    private String propertyId;

    private Long fromDate;

    private Long toDate;

}
