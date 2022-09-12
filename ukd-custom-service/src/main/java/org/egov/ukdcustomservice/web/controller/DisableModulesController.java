package org.egov.ukdcustomservice.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.egov.ukdcustomservice.service.DisableModulesService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DisableModulesController {
	
	
	private DisableModulesService disableModulesService;

	@PostMapping(value = "/endpoints/_noaccess")
	public ResponseEntity<Object> method(@RequestParam String tenantId, @RequestBody Object customRequest, HttpServletRequest request) {
		log.info(request.getRequestURI());
		log.info(tenantId);
		disableModulesService.restrictApis(tenantId, request.getRequestURI(), customRequest);
		HttpHeaders httpHeaders = new HttpHeaders();

		return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleError(Exception e) {
		log.error("EXCEPTION_WHILE_RESTRICT_ACCESS", e);
		HttpHeaders httpHeaders = new HttpHeaders();
		// httpHeaders.setLocation(UriComponentsBuilder.fromHttpUrl(defaultURL).build().encode().toUri());
		return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
	}

}
