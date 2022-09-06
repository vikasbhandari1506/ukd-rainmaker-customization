package org.egov.ukdcustomservice.web.models;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notifications {

    private String mobileNumber;
    private String ownerName;
    private String pendingAmount;
    private String propertyId;
    private String businessService;
    private String tenantId;
    private Map<String, String> ownerNameMobileNo = new HashMap<>();
    
    public Notifications(String tenantId, String propertyId, String pendingAmount) {
    	this.tenantId = tenantId;
    	this.propertyId = propertyId;
    	this.pendingAmount = pendingAmount;
    }
}
