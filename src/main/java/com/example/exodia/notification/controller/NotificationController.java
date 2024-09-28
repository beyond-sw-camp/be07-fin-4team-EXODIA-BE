package com.example.exodia.notification.controller;

import com.example.exodia.common.service.SseEmitters;
import com.example.exodia.user.domain.CustomUserDetails;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.CustomUserService;
import com.example.exodia.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final SseEmitters sseEmitters;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CustomUserService customUserService;

    @Autowired
    public NotificationController(SseEmitters sseEmitters, UserService userService, UserRepository userRepository, CustomUserService customUserService) {
        this.sseEmitters = sseEmitters;
        this.userService = userService;
        this.userRepository = userRepository;
        this.customUserService = customUserService;
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        // SecurityContextHolder에서 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("사용자 인증 정보를 찾을 수 없습니다.");
        }

        String userNum = authentication.getName(); // 로그인된 사용자의 userNum을 가져옴

        // CustomUserService를 사용하여 UserDetails를 통해 User 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) customUserService.loadUserByUsername(userNum);
        User user = userDetails.getUser(); // User 객체 가져오기

        // SSE 구독 처리
        SseEmitter emitter = sseEmitters.addEmitter(user.getUserNum());
        return emitter;
    }
}

