package com.example.exodia.common.service;

import com.example.exodia.notification.domain.Notification;
import com.example.exodia.notification.dto.NotificationDTO;
import com.example.exodia.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseEmitters {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter addEmitter(String userNum) {
        SseEmitter emitter = new SseEmitter(180_000L); // 3분 타임아웃
        emitters.put(userNum, emitter);

        emitter.onCompletion(() -> emitters.remove(userNum));
        emitter.onTimeout(() -> emitters.remove(userNum));
        emitter.onError(e -> {
            emitters.remove(userNum);
            e.printStackTrace();
        });

        return emitter;
    }
    // 모든 사용자
    public void sendToAll(Notification notification) {
        emitters.forEach((userNum, emitter) -> {
            try {
                emitter.send(SseEmitter.event().data(notification));
            } catch (IOException e) {
                emitters.remove(userNum);
            }
        });
    }

    public void sendToUser(String userNum, NotificationDTO dto) {
        SseEmitter emitter = emitters.get(userNum);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(dto));
                System.out.println("알림 전송 성공: " + userNum);
            } catch (IOException e) {
                emitters.remove(userNum);
                System.out.println("알림 전송 실패, SSE 연결 해제: " + userNum);
                e.printStackTrace();
            }
        } else {
            System.out.println("SSE 연결 없음: " + userNum);
        }
    }
}

