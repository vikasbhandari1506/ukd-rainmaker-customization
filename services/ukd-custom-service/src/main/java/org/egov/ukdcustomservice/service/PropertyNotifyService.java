package org.egov.ukdcustomservice.service;

import java.util.List;

import org.egov.ukdcustomservice.repository.PropertyNotifyRepository;
import org.egov.ukdcustomservice.web.models.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PropertyNotifyService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PropertyNotifyService.class);

    @Autowired
    private PropertyNotifyRepository propertyNotifyRepository;

    @Autowired
    private NotificationService notificationService;

    public String PTNotify(String tenantid) {
        List<Notifications> notifications = propertyNotifyRepository.getNotifications(tenantid);
        log.info("Total messages: " + notifications.size());
        notificationService.NotificationPush(notifications);
        return "Total messages: " + notifications.size();
    }

}