package org.egov.ukdcustomservice.web.contract;

import java.util.Set;

import org.egov.ukdcustomservice.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyCriteria {

	private String tenantId;

	private Set<String> propertyIds;
	
	private Set<String> acknowledgementIds;
	
	private Set<String> uuids;

	private Set<String> oldpropertyids;
	
	private Status status;

	private String mobileNumber;

	private String name;
	
	private Set<String> ownerIds;
	
	private boolean audit;
	
	private Long offset;

	private Long limit;
	
	private String locality;
	
	private String doorNo;

	
}
