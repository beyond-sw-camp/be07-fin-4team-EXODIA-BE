package com.example.exodia.common.service;

import com.example.exodia.notification.domain.Notification;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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

    public void sendToUser(String userNum, Notification notification) {
        SseEmitter emitter = emitters.get(userNum);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(notification));
            } catch (IOException e) {
                emitters.remove(userNum);
                e.printStackTrace();
            }
        }
    }
}

