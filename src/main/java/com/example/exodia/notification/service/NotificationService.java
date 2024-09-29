package com.example.exodia.notification.service;

import com.example.exodia.common.service.SseEmitters;
import com.example.exodia.notification.domain.Notification;
import com.example.exodia.notification.domain.NotificationType;
import com.example.exodia.notification.repository.NotificationRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public long countUnreadNotifications() {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    // 읽음 처리
    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림이 없습니다."));
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!notification.getUser().getUserNum().equals(userNum)) {
            throw new SecurityException("다른 사용자의 알림은 읽을 수 없습니다.");
        } // 오짜피 로그인 해서 개인 정보만 받아오기 때문에 포스트 맨 테스트를 위해서 삽입

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    // 인사팀 관리자에게 예약 요청 알림 전송
    public void sendReservationReqToAdmins(String message) {
        // 후 department_name 에 맞게 인사1팀 인사 2팀 이런식의 명칭 변경
        List<User> admins = userRepository.findAllByDepartmentName("인사팀");
        for (User admin : admins) {
            Notification notification = new Notification(admin, NotificationType.예약, message);
            notificationRepository.save(notification);

            // SSE 실시간 알림 전송
            sseEmitters.sendToUser(admin.getUserNum(), notification);
        }
    }

    // 관리자 승인 -> 사용자 알림(승리)
    public void sendReservationApproval(User user, String message) {
        Notification notification = new Notification(user, NotificationType.차량예약승인, message);
        notificationRepository.save(notification);
        sseEmitters.sendToUser(user.getUserNum(), notification);
    }

    // 관리자 거절 -> 사용자 알림(거절)
    public void sendReservationRejection(User user, String message) {
        Notification notification = new Notification(user, NotificationType.차량예약거절, message);
        notificationRepository.save(notification);
        sseEmitters.sendToUser(user.getUserNum(), notification);
    }

    // 관리자들에게 회의실 예약 요청 알림을 전송
    public void sendMeetReservationReqToAdmins(String message) {
        List<User> admins = userRepository.findAllByDepartmentName("인사팀");

        for (User admin : admins) {
            Notification notification = new Notification(admin, NotificationType.회의실예약, message);
            notificationRepository.save(notification);

            // SSE로 실시간 알림 전송
            sseEmitters.sendToUser(admin.getUserNum(), notification);
        }
    }
}
