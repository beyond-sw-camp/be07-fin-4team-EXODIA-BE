package com.example.exodia.notification.service;

import com.example.exodia.common.service.SseEmitters;
import com.example.exodia.notification.domain.Notification;
import com.example.exodia.notification.domain.NotificationType;
import com.example.exodia.notification.dto.NotificationDTO;
import com.example.exodia.notification.repository.NotificationRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SseEmitters sseEmitters;
    private final UserService userService;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, SseEmitters sseEmitters, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.sseEmitters = sseEmitters;
        this.userService = userService;
    }


    public List<NotificationDTO> getNotificationsByUser(String userNum) {

        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));

        // 해당 유저의 알림 리스트를 가져옴
        List<Notification> notifications = notificationRepository.findByUserId(user.getId());

        // Notification 엔티티를 DTO로 변환하여 반환
        return notifications.stream()
                .map(NotificationDTO::fromEntity)  // Notification -> NotificationDTO 변환
                .collect(Collectors.toList());
    }

    // 읽지 않은 알림의 개수를 반환
    public long countUnreadNotifications(String userNum) {
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));

        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    // 특정 알림을 읽음 처리
    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림이 존재하지 않습니다."));

        // 알림의 소유자가 현재 사용자와 일치하는지 확인
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!notification.getUser().getUserNum().equals(userNum)) {
            throw new SecurityException("다른 사용자의 알림을 읽음으로 처리할 수 없습니다.");
        }

        // 읽음 처리
        notification.markAsRead();
        notificationRepository.save(notification);
    }

}
