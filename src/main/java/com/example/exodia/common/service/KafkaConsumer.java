package com.example.exodia.common.service;

import com.example.exodia.notification.domain.Notification;
import com.example.exodia.notification.domain.NotificationType;
import com.example.exodia.notification.repository.NotificationRepository;
import com.example.exodia.notification.service.NotificationService;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
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
    public KafkaConsumer(NotificationRepository notificationRepository, NotificationService notificationService, UserRepository userRepository, SseEmitters sseEmitters) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.sseEmitters = sseEmitters;
    }

    // 데이터 중복 입고 처리 수정
    @Transactional
    @KafkaListener(topics = "notice-events", groupId = "notification-group")
    public void listen(String message) {
        System.out.println("listen: " + message);
        List<User> users = userRepository.findAll();

        NotificationType notificationType = message.contains("경조사") ? NotificationType.경조사 : NotificationType.공지사항;

        for (User user : users) {
            // 중복된 알림 방지
            boolean exists = notificationRepository.existsByUserAndMessage(user, message);
            if (!exists) {
                Notification notification = new Notification(user, notificationType, message);
                notificationRepository.save(notification);

                // SSE 실시간 알림 전송
                sseEmitters.sendToUser(user.getUserNum(), notification);
                System.out.println("SSE 이벤트 전송: " + user.getUserNum() + " " + message + " 메세지");
            }
        }
    }


}


