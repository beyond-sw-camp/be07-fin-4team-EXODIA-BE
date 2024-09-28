package com.example.exodia.common.service;

import com.example.exodia.notification.domain.Notification;
import com.example.exodia.notification.domain.NotificationType;
import com.example.exodia.notification.repository.NotificationRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaConsumer {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SseEmitters sseEmitters;

    @Autowired
    public KafkaConsumer(NotificationRepository notificationRepository, UserRepository userRepository, SseEmitters sseEmitters) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.sseEmitters = sseEmitters;
    }

    @KafkaListener(topics = "notice-events", groupId = "notification-group")
    public void listen(String message) {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            // 알림을 데이터베이스에 저장
            Notification notification = new Notification(user, NotificationType.공지사항, message);
            notificationRepository.save(notification);

            // SSE를 통해 실시간으로 알림 전송
            sseEmitters.sendToUser(user.getUserNum(), notification);
        }
    }
}


