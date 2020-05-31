package org.egov.ukdcustomservice.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import org.egov.ukdcustomservice.web.models.Pod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LogService {

    @Value("${kubernetes.service.host:localhost}")
    private String kubeHost;

    @Value("${kubernetes.port.443.tcp.port:8080}")
    private String kubePort;

    @Autowired
    RestTemplate restTemplate;

    public InputStreamResource getLogs(String name, String follow, String tail) {
        try {

            follow = (follow == null || follow.equals("")) ? "false" : follow;
            tail = (tail == null || tail.equals("")) ? "300" : tail;

            URL url = new URL(getLogsURL(name, follow, tail));
            HttpURLConnection myURLConnection = (HttpURLConnection) url.openConnection();
            myURLConnection.setRequestMethod("GET");
            myURLConnection.setConnectTimeout(3600000); // one hour
            myURLConnection.setRequestProperty("Authorization", "Bearer " + getToken());
            InputStreamResource inputStreamResource = new InputStreamResource(myURLConnection.getInputStream());

            return inputStreamResource;

        } catch (Exception e) {
            log.error("Error while getting logs", e);
            return null;
        }
    }

    public List<Pod> getPogs() {
        try {
            List<Pod> pods = new ArrayList<Pod>();
            String json = restTemplate.getForEntity(getPodsURL(), String.class).getBody();
            JSONObject jsonObject = new JSONObject(json);
            JSONArray items = jsonObject.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                String pod = items.getJSONObject(i).toString();

                pods.add(new Pod(JsonPath.read(pod, "$.metadata.name"),
                        JsonPath.read(pod, "$.status.containerStatuses[0].image"),
                        JsonPath.read(pod, "$.metadata.creationTimestamp"), JsonPath.read(pod, "$.status.phase")));
            }

            return pods;
        } catch (Exception e) {
            log.error("Error while getting pods", e);
            return null;
        }
    }

    private String getToken() throws IOException {
        return Files.readAllLines(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token")).get(0);
    }

    private String getLogsURL(String name, String follow, String tail) {
        return "https://" + kubeHost + ":" + kubePort + "/api/v1/namespaces/egov/pods/" + name + "/log?follow=" + follow
                + "&tailLines=" + tail;
    }

    private String getPodsURL() {
        return "https://" + kubeHost + ":" + kubePort + "/api/v1/namespaces/egov/pods";
    }

}
