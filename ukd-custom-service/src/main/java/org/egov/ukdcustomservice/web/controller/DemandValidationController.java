package org.egov.ukdcustomservice.web.controller;

import javax.validation.Valid;

import org.egov.ukdcustomservice.service.DemandValidationService;
import org.egov.ukdcustomservice.web.models.DemandRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demand")
public class DemandValidationController {

    @Autowired
    private DemandValidationService demandValidationService;
    
    @PostMapping("/_validate")
    public ResponseEntity<?> create(@RequestBody @Valid DemandRequest demandRequest) {   
		return demandValidationService.validate(demandRequest.getDemands());
	}

}