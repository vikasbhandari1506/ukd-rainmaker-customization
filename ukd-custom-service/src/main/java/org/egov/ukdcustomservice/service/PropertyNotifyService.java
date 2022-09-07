package org.egov.ukdcustomservice.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ukdcustomservice.repository.PropertyNotifyRepository;
import org.egov.ukdcustomservice.repository.ServiceRequestRepository;
import org.egov.ukdcustomservice.web.models.NotificationRequest;
import org.egov.ukdcustomservice.web.models.Notifications;
import org.egov.ukdcustomservice.web.models.OwnerInfo;
import org.egov.ukdcustomservice.web.models.PropertyResponse;
import org.egov.ukdcustomservice.web.models.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PropertyNotifyService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PropertyNotifyService.class);

    @Autowired
    private PropertyNotifyRepository propertyNotifyRepository;

    @Autowired
    private NotificationService notificationService;
    
    @Value("${egov.pt.registry.host}")
    private String propertyHost;
    
    @Value("${egov.pt.registry.search}")
    private String propertySearch;
    
    @Autowired
    private ServiceRequestRepository repository;
    
    @Autowired
    private ObjectMapper mapper;

    public String PTNotify(NotificationRequest notificationRequest, RequestInfo requestInfo) {
    	List<Object> preparedStmtList = new ArrayList<>();
        List<Notifications> notifications = propertyNotifyRepository.getNotifications(notificationRequest, preparedStmtList);
        log.info("Total messages: {}", notifications.size());
        if(!notifications.isEmpty()) {
        	RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        	Pattern ptrn = Pattern.compile("(^$|[6-9][0-9]{9})");
            for(Notifications notifys: notifications) {
            	StringBuilder url = new StringBuilder(propertyHost);
                url.append(propertySearch);
                url.append("?tenantId=");
                url.append(notifys.getTenantId());
                url.append("&");
                url.append("propertyIds=");
                url.append(notifys.getPropertyId());
                
                Object obj = repository.fetchResult(url, requestInfoWrapper);
                try {
					log.info(mapper.writeValueAsString(obj));
				} catch (JsonProcessingException e) {
					log.error("Error occurred on parsing::: {}", e.getMessage());
				}
                PropertyResponse propertyResponse = mapper.convertValue(obj, PropertyResponse.class);
                log.info("propertyResponse:: {}", propertyResponse.toString());
                Iterator<OwnerInfo> ownerItr = propertyResponse.getProperties().get(0).getOwners().iterator();
                while(ownerItr.hasNext()) {
                	OwnerInfo owner = ownerItr.next();
                	Matcher match = ptrn.matcher(owner.getMobileNumber());  
                	// Send SMS only to valid mobile numbers
                	if(match.find() && match.group().equals(owner.getMobileNumber()))
                		notifys.getOwnerNameMobileNo().put(owner.getMobileNumber(), owner.getName());
                }
            }
        }
        
        notificationService.NotificationPush(notifications, "PT", requestInfo, notificationRequest);
        return "SMS Sent Successfully to the valid mobile numbers only!!!!";
    }

}