package org.egov.ukdcustomservice.service;

import java.util.Calendar;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ukdcustomservice.producer.Producer;
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

    @Value("${egov.notify.pt.message.key}")
    private String ptKey;

    @Value("${egov.notify.pt.message.module}")
    private String ptModule;

    @Value("${egov.notify.domain}")
    private String domainName;

    @Value("${egov.notify.shouldPush}")
    private boolean shouldPush;

    public void NotificationPush(List<Notifications> notifications, String key, RequestInfo requestInfo) {

        SMS sms = new SMS();

        String message = getMessage(key, requestInfo);
        String ulbname = getTenant(notifications.get(0).getTenantId(), requestInfo);

        notifications.forEach(val -> {
            sms.setMobileNumber(val.getMobileNumber());
            String content = message.replace("<ownername>", val.getOwnerName());
            content = content.replace("<taxamount>", val.getPendingAmount());
            content = content.replace("<domain>", domainName);
            content = content.replace("<propertyid>", val.getConsumerNumber());
            content = content.replace("<tenantid>", val.getTenantId());
            content = content.replace("<FY>", getFY());
            content = content.replace("<ulbname>", ulbname);

            sms.setMessage(content);
            log.info(val.getMobileNumber() + " " + content);

            // format the message
            if (shouldPush)
                producer.pushToSMSTopic(sms);
        });

    }

    private String getTenant(String tenantid, RequestInfo requestInfo ) {       
       return localizationService.getResult("TENANT_TENANTS_".concat(tenantid.replace(".","_").toUpperCase()), "rainmaker-common", requestInfo);
    }

    private String getMessage(String key, RequestInfo requestInfo) {

        String message = ""; 

        if(key.equals("PT")){
            message = localizationService.getResult(ptKey, ptModule, requestInfo);
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