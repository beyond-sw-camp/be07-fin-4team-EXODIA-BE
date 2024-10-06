package com.example.exodia.common.service;

import com.example.exodia.notification.domain.Notification;
import com.example.exodia.notification.domain.NotificationType;
import com.example.exodia.notification.dto.NotificationDTO;
import com.example.exodia.notification.repository.NotificationRepository;
import com.example.exodia.notification.service.NotificationService;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KafkaConsumer {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final SseEmitters sseEmitters;

    @Autowired
    public KafkaConsumer(NotificationRepository notificationRepository, NotificationService notificationService,
                         UserRepository userRepository, SseEmitters sseEmitters) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.sseEmitters = sseEmitters;
    }

    @Transactional
    @KafkaListener(topics = {"notice-events", "document-events", "submit-events"}, groupId = "notification-group")
    public void listen(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic, String message) {
        System.out.println("Kafka 메시지 수신: " + message);

        switch (topic) {
            case "document-events":
                processDocumentUpdateMessage(message);
                break;
            case "notice-events":
                processBoardNotification(message);
                break;
            case "submit-events":
                processSubmitNotification(message);
                break;
            default:
                System.out.println("알 수 없는 토픽이거나 메시지 형식이 맞지 않습니다.");
        }
    }

    // 문서 업데이트 메시지 처리
    private void processDocumentUpdateMessage(String message) {
        if (message.contains("|")) {
            String[] splitMessage = message.split("\\|", 2);
            String departmentId = splitMessage[0];
            String actualMessage = splitMessage[1];

            List<User> departmentUsers = userRepository.findAllByDepartmentId(Long.parseLong(departmentId));
            for (User user : departmentUsers) {
                boolean exists = notificationRepository.existsByUserAndMessage(user, actualMessage);
                if (!exists) {
                    Notification notification = new Notification(user, NotificationType.문서, actualMessage);
                    notificationRepository.save(notification);
//                    sseEmitters.sendToUser(user.getUserNum(), notification);

                    NotificationDTO dto = new NotificationDTO(notification);
                    sseEmitters.sendToUser(user.getUserNum(), dto);
                }
            }
        }
    }

    // 공지사항 알림 처리
    private void processBoardNotification(String message) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            boolean exists = notificationRepository.existsByUserAndMessage(user, message);
            if (!exists) {
                Notification notification = new Notification(user, NotificationType.공지사항, message);
                notificationRepository.save(notification);

                NotificationDTO dto = new NotificationDTO(notification);
                sseEmitters.sendToUser(user.getUserNum(), dto);
            }
        }
    }

    // 결재 알림 처리
    private void processSubmitNotification(String message) {
        // 메시지 형식: "departmentId|submitId|결재 내용"
        if (message.contains("|")) {
            String[] splitMessage = message.split("\\|", 3);
            String departmentId = splitMessage[0];
            String submitId = splitMessage[1];
            String submitMessage = splitMessage[2];

            // 결재자들 필터링 및 알림 전송
            List<User> departmentUsers = userRepository.findAllByDepartmentId(Long.parseLong(departmentId));
            for (User user : departmentUsers) {
                boolean exists = notificationRepository.existsByUserAndMessage(user, submitMessage);
                if (!exists) {
                    Notification notification = new Notification(user, NotificationType.결재, submitMessage);
                    notificationRepository.save(notification);
//                    sseEmitters.sendToUser(user.getUserNum(), notification);

                    NotificationDTO dto = new NotificationDTO(notification);
                    sseEmitters.sendToUser(user.getUserNum(), dto);
                }
            }
        }
    }
}



