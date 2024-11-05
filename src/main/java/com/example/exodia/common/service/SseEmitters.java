package com.example.exodia.common.service;

import com.example.exodia.chat.dto.ChatAlarmResponse;

import com.example.exodia.notification.dto.NotificationDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseEmitters {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, NotificationDTO> cache = new WeakHashMap<>();

    public SseEmitter addEmitter(String userNum) {
        SseEmitter emitter = new SseEmitter(3600_000L);
        emitters.put(userNum, emitter);

        // SSE 종료 처리
        emitter.onCompletion(() -> {
            emitters.remove(userNum);
            cache.remove(userNum);
            System.out.println("SSE 연결 완료: " + userNum);
        });

        // SSE 타임아웃 처리
        emitter.onTimeout(() -> {
            emitters.remove(userNum);
            cache.remove(userNum);
            System.out.println("SSE 연결 타임아웃 발생: " + userNum);
        });

        // SSE 오류 처리
        emitter.onError(e -> {
            emitters.remove(userNum);
            cache.remove(userNum);
            System.out.println("SSE 연결 오류 발생: " + userNum);
        });

        return emitter;
    }

    // 모든 사용자에게 알림 전송
    public void sendToAll(NotificationDTO notificationDTO) {
        emitters.forEach((userNum, emitter) -> {
            try {
                emitter.send(SseEmitter.event().data(notificationDTO));
            } catch (IOException e) {
                emitters.remove(userNum);
                cache.remove(userNum);
                System.out.println("SSE 연결 해제: " + userNum);
            }
        });
    }

    // 특정 사용자에게 알림 전송
    public void sendToUser(String userNum, NotificationDTO dto) {
        SseEmitter emitter = emitters.get(userNum);
        cache.put(userNum, dto);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(dto));
                System.out.println("알림 전송 성공: " + userNum);
            } catch (IOException e) {
                emitters.remove(userNum);
                cache.remove(userNum);
                System.out.println("알림 전송 실패, SSE 연결 해제: " + userNum);
            }
        }
    }

    // 특정 사용자에게 채팅 알림 전송
    public void sendChatToUser(String userNum, ChatAlarmResponse dto) {
        SseEmitter emitter = emitters.get(userNum);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(dto));
                System.out.println("채팅 알림 전송 성공: " + userNum);
            } catch (IOException e) {
                emitters.remove(userNum);
                cache.remove(userNum);
                System.out.println("채팅 알림 전송 실패, SSE 연결 해제: " + userNum);
            }
        } else {
            System.out.println("SSE 연결 없음: " + userNum);
        }
    }

    @Scheduled(fixedRate = 15000)
    public void sendHeartbeat() {
        emitters.forEach((userNum, emitter) -> {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
                System.out.println("Heartbeat 전송: " + userNum);
            } catch (IOException e) {
                emitters.remove(userNum);
                cache.remove(userNum);
                System.out.println("Heartbeat 전송 실패, SSE 연결 해제: " + userNum);
            }
        });
    }
}
