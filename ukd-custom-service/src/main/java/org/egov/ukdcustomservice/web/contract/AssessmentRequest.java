package org.egov.ukdcustomservice.web.contract;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ukdcustomservice.models.Assessment;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The request to create or update an assessment. Contains the RequestHeader and the assessment details
 */

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentRequest   {

  @JsonProperty("RequestInfo")
  private RequestInfo  requestInfo;

  @Valid
  @NotNull
  @JsonProperty("Assessment")
  private Assessment assessment;
}
