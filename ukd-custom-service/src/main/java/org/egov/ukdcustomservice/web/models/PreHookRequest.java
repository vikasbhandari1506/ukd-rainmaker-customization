package org.egov.ukdcustomservice.web.models;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PreHookRequest {
    @NotNull
	@JsonProperty("request")
	private String request;
}