package org.egov.ukdcustomservice.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.egov.tracer.kafka.CustomKafkaTemplate;

@Service
public class Producer {

    @Autowired
    private CustomKafkaTemplate<String, Object> kafkaTemplate;

    public void push(String topic, Object value) {
        kafkaTemplate.send(topic, value);
    }

    public void pushToSMSTopic(Object value) {
        kafkaTemplate.send("sms.topic", value);
    }
}