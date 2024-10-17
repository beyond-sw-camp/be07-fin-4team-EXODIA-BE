package com.example.exodia.common.service;

import com.example.exodia.chat.dto.ChatAlarmResponse;
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
        SseEmitter emitter = new SseEmitter(180_000L); // 무제한 타임아웃
        emitters.put(userNum, emitter);

        // SSE 종료/에러 -> 백업처리로직
        emitter.onCompletion(() -> emitters.remove(userNum));
        emitter.onTimeout(() -> {
            emitters.remove(userNum);
            System.out.println("SSE 연결 타임아웃 발생: " + userNum);
        });
        emitter.onError(e -> {
            emitters.remove(userNum);
            System.out.println("SSE 연결 오류 발생: " + userNum);
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

    public void sendChatToUser(String userNum, ChatAlarmResponse dto){
        SseEmitter emitter = emitters.get(userNum);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("chatAlarm").data(dto));
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

