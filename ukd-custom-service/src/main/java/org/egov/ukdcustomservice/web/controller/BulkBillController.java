package org.egov.ukdcustomservice.web.controller;

import java.util.Map;

import javax.validation.Valid;

import org.egov.ukdcustomservice.models.GenerateBillCriteria;
import org.egov.ukdcustomservice.service.BulkBillService;
import org.egov.ukdcustomservice.web.contract.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class BulkBillController {

    @Autowired
    private BulkBillService bulkBillService;
    
    @RequestMapping(value = "/v1/bulkbill/_generate", method = RequestMethod.POST )
    public ResponseEntity<Object> rollOver(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
    		@ModelAttribute @Valid GenerateBillCriteria generateBillCriteria) {
    	Map<String, String> resultMap = null;

		resultMap=	bulkBillService.generateBulkBills(requestInfoWrapper,generateBillCriteria); 
		
		return new ResponseEntity<>(resultMap, HttpStatus.OK);}

}
