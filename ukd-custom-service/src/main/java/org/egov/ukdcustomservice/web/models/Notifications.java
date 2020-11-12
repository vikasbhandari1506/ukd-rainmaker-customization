package org.egov.ukdcustomservice.web.models;

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
    private String consumerNumber;
    private String businessService;
    private String tenantId;
    // Period of pending amount

}
