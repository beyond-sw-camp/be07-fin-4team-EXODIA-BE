package com.example.exodia.notification.controller;

import com.example.exodia.common.service.SseEmitters;
import com.example.exodia.notification.service.NotificationService;
import com.example.exodia.user.domain.CustomUserDetails;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.CustomUserService;
import com.example.exodia.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final SseEmitters sseEmitters;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CustomUserService customUserService;
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(SseEmitters sseEmitters, UserService userService, UserRepository userRepository, CustomUserService customUserService, NotificationService notificationService) {
        this.sseEmitters = sseEmitters;
        this.userService = userService;
        this.userRepository = userRepository;
        this.customUserService = customUserService;
        this.notificationService = notificationService;
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("사용자 인증 정보를 찾을 수 없습니다.");
        }
        String userNum = authentication.getName();
        CustomUserDetails userDetails = (CustomUserDetails) customUserService.loadUserByUsername(userNum);
        User user = userDetails.getUser();

        SseEmitter emitter = sseEmitters.addEmitter(user.getUserNum());
        return emitter;
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount() {
        // 서비스 호출하여 읽지 않은 알림 수 반환
        long unreadCount = notificationService.countUnreadNotifications();

        return ResponseEntity.ok(unreadCount);
    }

    // 특정 알림을 읽음 처리
    @PostMapping("/mark-as-read/{id}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        // 서비스 호출하여 알림 읽음 처리
        notificationService.markNotificationAsRead(id);

        return ResponseEntity.ok().build();
    }
}