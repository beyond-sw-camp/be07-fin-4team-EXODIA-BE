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


//    public void sendBoardEvent(String topic, String message) {
//        String uniqueKey = UUID.randomUUID().toString();
//        kafkaTemplate.send(topic, message);
//        System.out.println("Kafka 이벤트 : " + message); //
//        System.out.println(topic);
//    }
    // 게시판 알림
    public void sendBoardEvent(String topic, String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("Kafka 이벤트 전송: " + message + " / 토픽: " + topic);
    }
    // 결재 알림 전송
    public void sendSubmitNotification(String topic, String userNum, String message) {
        kafkaTemplate.send(topic, userNum, message);
        System.out.println("Kafka 결재 알림 전송: " + message + " / 유저 넘버: " + userNum);
    }

    // 문서 업데이트 알림을 위한 메서드 (부서 ID 포함)
    public void sendDocumentUpdateEvent(String topic, String documentName, String userName, String departmentId, String date) {
        String message = String.format("%s 님이 문서를 업데이트 했습니다: %s (%s)", userName, documentName, date);
        kafkaTemplate.send(topic, departmentId + "|" + message);
        System.out.println("Kafka 문서 업데이트 이벤트: " + message);
    }


    // 강좌
    public void sendCourseRegistrationEvent(String courseId, String message) {
        kafkaTemplate.send("course-registration", courseId, message);
        System.out.println("Kafka 강좌 등록 이벤트 전송: " + message + " / 코스 ID: " + courseId);
    }
}

