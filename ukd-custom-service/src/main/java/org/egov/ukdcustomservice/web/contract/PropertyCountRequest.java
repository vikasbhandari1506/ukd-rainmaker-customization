package org.egov.ukdcustomservice.web.contract;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ukdcustomservice.models.PropertyCount;
import org.egov.ukdcustomservice.models.RollOverCount;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Contract class to receive request. Array of Property items  are used in case of create . Where as single Property item is used for update
 */

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyCountRequest {
	
  @JsonProperty("RequestInfo")
  private RequestInfo requestInfo;

  @JsonProperty("PropertyCount")
  @Valid
  private PropertyCount propertyCount;
}
