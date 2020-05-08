package org.egov.ukdcustomservice.service;

import java.util.List;

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

    @Value("${egov.notify.pt.message}")
    private String message;

    @Value("${egov.notify.domain}")
    private String domainName;

    @Value("${egov.notify.shouldPush}")
    private boolean shouldPush;

    public void NotificationPush(List<Notifications> notifications) {

        SMS sms = new SMS();

        notifications.forEach(val -> {
            sms.setMobileNumber(val.getMobileNumber());
            String content = message.replace("<ownername>", val.getOwnerName());
            content = content.replace("<taxamount>", val.getPendingAmount());
            content = content.replace("<domain>", domainName);
            content = content.replace("<propertyid>", val.getConsumerNumber());
            content = content.replace("<tenantid>", val.getTenantId());

            sms.setMessage(content);
            log.info(val.getMobileNumber() + " " + content);

            // format the message
            if (shouldPush)
                producer.pushToSMSTopic(sms);
        });

    }

}