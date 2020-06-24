package org.egov.ukdcustomservice.service;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import org.egov.common.contract.request.RequestInfo;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

    @Value("${egov.localization.language}")
    private String language;

    public String getResult(String key, String module, RequestInfo requestInfo) {

        StringBuilder uri = new StringBuilder();

        uri.append(localizartionHost).append(endpoint).append("?tenantId=uk").append("&module=").append(module)
                .append("&locale=").append(language);
        ArrayList<String> message = new ArrayList<String>();
        Map response = null;
        log.info("URI: " + uri.toString());
        try {
            log.info("Request: " + mapper.writeValueAsString(requestInfo));
            response = restTemplate.postForObject(uri.toString(), requestInfo, Map.class);
            String jsonString = new JSONObject(response).toString();
            log.info(jsonString);
            message = (ArrayList<String>) JsonPath.parse(jsonString)
                    .read("$.messages[?(@.code=='" + key + "')].message");

        } catch (Exception e) {
            log.error("Exception while fetching from searcher: ", e);
        }

        return message.size() >= 1 ? message.get(0) : null;
    }

}