package org.egov.ukdcustomservice.models;


import lombok.Data;

@Data
public class DemandSearchCriteria {

    private String tenantId;

    private String propertyId;

    private Long fromDate;

    private Long toDate;

}
