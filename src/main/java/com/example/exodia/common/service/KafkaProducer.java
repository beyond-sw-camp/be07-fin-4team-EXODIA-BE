package com.example.exodia.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    public void sendBoardEvent(String topic, String message) {
        String uniqueKey = UUID.randomUUID().toString();
        kafkaTemplate.send(topic, message);
        System.out.println("Kafka 이벤트 : " + message); //
        System.out.println(topic);
    }
}

