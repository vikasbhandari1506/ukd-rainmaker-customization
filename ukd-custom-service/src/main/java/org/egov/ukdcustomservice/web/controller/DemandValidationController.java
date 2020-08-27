package org.egov.ukdcustomservice.web.controller;


import org.egov.ukdcustomservice.web.models.DemandRequest;
import org.springframework.http.HttpStatus;
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

    // @Autowired
    // private DemandValidationService demandValidationService;
    
    @PostMapping("/_validate")
    public ResponseEntity<?> create(@RequestBody DemandRequest demandRequest) {  
     log.info("DemandRequest: " + demandRequest);
    // return demandValidationService.validate(demandRequest.getDemands());
    return new ResponseEntity<>(true, HttpStatus.OK);
	}

}