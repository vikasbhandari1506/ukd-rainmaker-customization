package org.egov.ukdcustomservice.web.models;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotificationRequest {

	private Set<String> propertyId;
    private String mobileNumber;
    private String ownerName;
    private String pendingAmount;
    private String businessService;
    private String tenantId;
	private Long offset;
	private Long limit;
	private String locale;

}
