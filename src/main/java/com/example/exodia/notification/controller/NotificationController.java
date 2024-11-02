package com.example.exodia.notification.controller;

import com.example.exodia.common.auth.JwtTokenProvider;
import com.example.exodia.common.service.SseEmitters;
import com.example.exodia.notification.dto.NotificationDTO;
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

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final SseEmitters sseEmitters;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CustomUserService customUserService;
    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public NotificationController(SseEmitters sseEmitters, UserService userService, UserRepository userRepository, CustomUserService customUserService, NotificationService notificationService, JwtTokenProvider jwtTokenProvider) {
        this.sseEmitters = sseEmitters;
        this.userService = userService;
        this.userRepository = userRepository;
        this.customUserService = customUserService;
        this.notificationService = notificationService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam("token") String token) {
        // 토큰을 검증
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new SecurityException("유효하지 않은 토큰입니다.");
        }

        // 토큰에서 사용자 정보 추출
        String userNum = jwtTokenProvider.getUserNumFromToken(token);

        // 사용자 정보 확인
        CustomUserDetails userDetails = (CustomUserDetails) customUserService.loadUserByUsername(userNum);
        User user = userDetails.getUser();

        // 사용자에 대한 SSE Emitter 생성
        SseEmitter emitter = sseEmitters.addEmitter(user.getUserNum());
        return emitter;
    }

    /* 사용자의 모든 알림 조회 */
    @GetMapping("/{userNum}")
    public ResponseEntity<List<NotificationDTO>> getNotifications(@PathVariable String userNum) {
        List<NotificationDTO> notifications = notificationService.getNotifications(userNum);
        return ResponseEntity.ok(notifications);
    }

    // 특정 알림을 읽음 처리
    @PutMapping("/{userNum}/read/{notificationId}")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable String userNum, @PathVariable String notificationId) {
        notificationService.markNotificationAsRead(userNum, notificationId);
        return ResponseEntity.ok().build();
    }

    // 알림의 읽음 여부 조회
    @GetMapping("/{userNum}/isRead/{notificationId}")
    public ResponseEntity<Boolean> isNotificationRead(@PathVariable String userNum, @PathVariable String notificationId) {
        boolean isRead = notificationService.isNotificationRead(userNum, notificationId);
        return ResponseEntity.ok(isRead);
    }



//    // 사용자별 알림 리스트를 가져오는 API
//    @GetMapping("/list")
//    public ResponseEntity<List<NotificationDTO>> getUserNotifications() {
//        // 사용자 인증 정보를 가져옴 (서비스에서 처리)
//        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
//
//        // 사용자별 알림 리스트 가져오기
//        List<NotificationDTO> notificationDTOs = notificationService.getNotificationsByUser(userNum);
//
//        return ResponseEntity.ok(notificationDTOs);
//    }
//
//    // 안읽은 알림의 개수를 가져오는 API
//    @GetMapping("/unread-count")
//    public ResponseEntity<Long> getUnreadNotificationCount() {
//        // 사용자 인증 정보를 가져옴
//        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
//
//        // 읽지 않은 알림의 개수를 서비스에서 처리
//        long unreadCount = notificationService.countUnreadNotifications(userNum);
//
//        return ResponseEntity.ok(unreadCount);
//    }
//
//    // 알림을 읽음 처리
//    @PostMapping("/mark-as-read/{id}")
//    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long id) {
//        // 서비스에서 읽음 처리
//        notificationService.markNotificationAsRead(id);
//
//        return ResponseEntity.ok().build();
//    }
}