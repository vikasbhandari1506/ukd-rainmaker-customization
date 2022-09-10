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
import org.egov.ukdcustomservice.web.models.OwnerInfo.OwnerStatus;
import org.egov.ukdcustomservice.web.models.PropertyResponse;
import org.egov.ukdcustomservice.web.models.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    	if(notificationRequest.isEmpty())
    		return "The TenantID is Mandatory. Please enter TenantID value.";
    	if(notificationRequest.getTenantId() != null && notificationRequest.getTenantId().split(".").length == 1)
    		return "Invalid TenantID. Please enter full TenantID value.";
    	List<Object> preparedStmtList = new ArrayList<>();
        List<Notifications> notifications = propertyNotifyRepository.getNotifications(notificationRequest, preparedStmtList);
        log.info("Total messages: {}", notifications.size());
        int totalSentSMS = 0;
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
                PropertyResponse propertyResponse = mapper.convertValue(obj, PropertyResponse.class);
                Iterator<OwnerInfo> ownerItr = propertyResponse.getProperties().get(0).getOwners().iterator();
                while(ownerItr.hasNext()) {
                	OwnerInfo owner = ownerItr.next();
                	Matcher match = ptrn.matcher(owner.getMobileNumber());
                	log.info("Mobile Number!!!!! {} and Status {} and isvalid {}", owner.getMobileNumber(), owner.getStatus().name(), !owner.getMobileNumber().equals("9999999999"));
                	// Send SMS only to valid mobile numbers
					if (match.find() && match.group().equals(owner.getMobileNumber())
							&& !owner.getMobileNumber().equals("9999999999")
							&& owner.getStatus().equals(OwnerStatus.ACTIVE)) {
                		notifys.getOwnerNameMobileNo().put(owner.getMobileNumber(), owner.getName());
                		totalSentSMS++;
					}
                }
            }
        }
        
        notificationService.NotificationPush(notifications, "PT", requestInfo, notificationRequest);
        if(totalSentSMS == 0)
        	return "No message sent. No valid mobile numbers found!!!!!";
        return String.format("SMS Sent Successfully to the valid mobile numbers only!!!! %d", totalSentSMS);
    }

}