package org.egov.ukdcustomservice.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ukdcustomservice.producer.Producer;
import org.egov.ukdcustomservice.web.models.NotificationRequest;
import org.egov.ukdcustomservice.web.models.Notifications;
import org.egov.ukdcustomservice.web.models.SMS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private Producer producer;

    @Autowired
    private LocalizationService localizationService;

    @Autowired
    private URLShorterService urlShorterService;

    @Value("${egov.notify.pt.message.key}")
    private String ptKey;

    @Value("${egov.notify.pt.message.module}")
    private String ptModule;

    @Value("${egov.notify.domain}")
    private String domainName;

    @Value("${egov.notify.shouldPush}")
    private boolean shouldPush;

    @Value("${egov.notify.pt.url.format}")
    private String urlFormat;

    public void NotificationPush(List<Notifications> notifications, String key, RequestInfo requestInfo, NotificationRequest notificationRequest) {

        SMS sms = new SMS();

        notifications.forEach(val -> {
			if (val.getPendingAmount() != null && new BigDecimal(val.getPendingAmount()).compareTo(BigDecimal.valueOf(10)) > 0
					&& val.getOwnerNameMobileNo() != null && !val.getOwnerNameMobileNo().isEmpty())
	        	for(Map.Entry<String, String> nameMob : val.getOwnerNameMobileNo().entrySet()) {
	        		String message = getMessage(key, requestInfo, notificationRequest);
	        		sms.setMobileNumber(nameMob.getKey());
	                String longURL = String.format(urlFormat, domainName, val.getPropertyId(), val.getTenantId());
	                String url = urlShorterService.getUrl(longURL);
	                log.info("Shorth url: {}", url);
	                String content = message.replace("{ownername}", nameMob.getValue());
	                content = content.replace("{domain}", url);
	                content = content.replace("{propertyid}", val.getPropertyId());
	                content = content.replace("{tenantid}", val.getTenantId());
	                content = content.replace("{FY}", getFY());
	                content = content.replace("{ulbname}", val.getTenantId());
	
	                sms.setMessage(content);
	                log.info(nameMob.getKey() + " " + content);
	
	                // format the message
	                if (shouldPush)
	                    producer.pushToSMSTopic(sms);
	        	}
        });

    }

	/*
	 * private String getTenant(String tenantid, RequestInfo requestInfo) { return
	 * localizationService.getResult("TENANT_TENANTS_".concat(tenantid.replace(".",
	 * "_").toUpperCase()), "rainmaker-common", requestInfo); }
	 */

    private String getMessage(String key, RequestInfo requestInfo, NotificationRequest notificationRequest) {

        String message = "";
        log.info(ptKey);
        if (key.equals("PT")) {
            message = localizationService.getResult(ptKey, ptModule, requestInfo, notificationRequest);
        }

        return message;
    }

    private String getFY() {

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;

        if (month < 3) {
            return (year - 1) + "-" + year;
        } else {
            return year + "-" + (year + 1);
        }
    }
}