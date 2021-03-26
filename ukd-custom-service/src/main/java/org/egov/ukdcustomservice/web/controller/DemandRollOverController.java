package org.egov.ukdcustomservice.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.egov.ukdcustomservice.service.RollOverService;
import org.egov.ukdcustomservice.web.contract.PropertyCriteria;
import org.egov.ukdcustomservice.web.contract.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DemandRollOverController {

    @Autowired
    private RollOverService rollOverService;
    
    @RequestMapping(value = "/v1/pt/demand/_rollover", method = RequestMethod.POST )
    public ResponseEntity<Object> rollOver(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			  @Valid @ModelAttribute PropertyCriteria propertyCriteria, @RequestParam List<String> tenantIdList) {

    	long startTime = System.nanoTime();
		Map<String, String> resultMap = null;
		Map<String, String> errorMap = new HashMap<>();

		resultMap = rollOverService.initiateProcess(requestInfoWrapper,propertyCriteria,errorMap, tenantIdList);

		long endtime = System.nanoTime();
		long elapsetime = endtime - startTime;
		System.out.println("Elapsed time--->"+elapsetime);
		
		return new ResponseEntity<>(resultMap, HttpStatus.OK);}

}
