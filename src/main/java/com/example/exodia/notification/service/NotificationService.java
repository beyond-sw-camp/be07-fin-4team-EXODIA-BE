package com.example.exodia.notification.service;

import com.example.exodia.common.service.SseEmitters;
import com.example.exodia.notification.domain.Notification;
import com.example.exodia.notification.domain.NotificationType;
import com.example.exodia.notification.repository.NotificationRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SseEmitters sseEmitters;
    private final UserService userService;//

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, SseEmitters sseEmitters, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.sseEmitters = sseEmitters;
        this.userService = userService;
    }

    public List<Notification> getUnreadNotifications(String userNum) {
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));
        return notificationRepository.findByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림이 없습니다."));
        notification.markAsRead();
    }

    // 관리자가 차량 예약 요청을 받았을 때 알림 전송
    // 인사팀 관리자에게 예약 요청 알림 전송
    public void sendReservationReqToAdmins(String message) {
        List<User> admins = userRepository.findAllByDepartmentName("인사팀");
        for (User admin : admins) {
            Notification notification = new Notification(admin, NotificationType.예약, message);
            notificationRepository.save(notification);

            // SSE 실시간 알림 전송
            sseEmitters.sendToUser(admin.getUserNum(), notification);
        }
    }

    // 사용자가 예약 승인 알림을 받을 때
    public void sendReservationApproval(User user, String message) {
        Notification notification = new Notification(user, NotificationType.차량예약승인, message);
        notificationRepository.save(notification);
        sseEmitters.sendToUser(user.getUserNum(), notification);
    }

    // 사용자가 예약 거절 알림을 받을 때
    public void sendReservationRejection(User user, String message) {
        Notification notification = new Notification(user, NotificationType.차량예약거절, message);
        notificationRepository.save(notification);
        sseEmitters.sendToUser(user.getUserNum(), notification);
    }
}
