package org.egov.ukdcustomservice.web.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.egov.tracer.model.CustomException;
import org.egov.ukdcustomservice.service.DemandValidationService;
import org.egov.ukdcustomservice.web.models.DemandRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/demand")
@Slf4j
public class DemandValidationController {

    @Autowired
    private DemandValidationService demandValidationService;

    @PostMapping("/_validate")
    public ResponseEntity<?> create(@RequestBody String jsonString) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DemandRequest demandRequest = null;
        try {
            demandRequest = objectMapper.readValue(jsonString, DemandRequest.class);
        } catch (Exception e) {
            log.error("Error while converting the input to demand request", e);
            new CustomException("JSON_CONVERSION_EXCEPTION", "Error while converting the input to demand request");
        }
        log.info("DemandRequest: " + demandRequest);
        return demandValidationService.validate(demandRequest.getDemands());
    }

}