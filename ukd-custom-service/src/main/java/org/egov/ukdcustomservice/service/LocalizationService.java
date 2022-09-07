package org.egov.ukdcustomservice.service;

import java.util.ArrayList;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ukdcustomservice.web.models.NotificationRequest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LocalizationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocalizationService.class);

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${egov.localization.host}")
    private String localizartionHost;

    @Value("${egov.localization.search.endpoint}")
    private String endpoint;

    @Value("${egov.pt.unpaid.sms.locale}")
    private String language;

    public String getResult(String key, String module, RequestInfo requestInfo, NotificationRequest notificationRequest) {

        StringBuilder uri = new StringBuilder();
        String locale = language;
        log.info("locale: {}",locale);
        if(notificationRequest.getLocale() != null)
        	locale = notificationRequest.getLocale();
        uri.append(localizartionHost).append(endpoint).append("?tenantId=uk").append("&module=").append(module)
                .append("&locale=").append(locale);
        ArrayList<String> message = new ArrayList<>();
        Map response = null;
        log.info("URI: " + uri.toString());
        try {
            log.info("Request: " + mapper.writeValueAsString(requestInfo));
            response = restTemplate.postForObject(uri.toString(), requestInfo, Map.class);
            String jsonString = new JSONObject(response).toString();
            message = (ArrayList<String>) JsonPath.parse(jsonString)
                    .read("$.messages[?(@.code=='" + key + "')].message");

        } catch (Exception e) {
            log.error("Exception while fetching from searcher: ", e);
        }

        return message.isEmpty() ? null : message.get(0);
    }

}