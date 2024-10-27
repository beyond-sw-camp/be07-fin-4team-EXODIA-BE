package com.example.exodia.common.service;

import com.example.exodia.chat.dto.ChatAlarmResponse;
import com.example.exodia.notification.domain.Notification;
import com.example.exodia.notification.dto.NotificationDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class SseEmitters {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public SseEmitter addEmitter(String userNum) {
        SseEmitter emitter = new SseEmitter(600_000L);
        emitters.put(userNum, emitter);
        System.out.println("SSE Emitter 추가: " + userNum);

        // Heartbeat 스케줄 설정
        ScheduledFuture<?> heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                // 연결이 유지되어 있으면 heartbeat 전송
                if (emitters.containsKey(userNum)) {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                } else {
                    throw new IOException("연결이 종료되었습니다.");
                }
            } catch (IOException e) {
                emitters.remove(userNum);
                System.out.println("SSE 연결 오류 발생 및 종료: " + userNum);
            }
        }, 0, 30, TimeUnit.SECONDS);

        // SSE 종료 처리
        emitter.onCompletion(() -> {
            emitters.remove(userNum);
            System.out.println("SSE 연결 완료: " + userNum);
            heartbeatTask.cancel(true); // heartbeat 스케줄 정리
        });

        // SSE 타임아웃 처리
        emitter.onTimeout(() -> {
            emitters.remove(userNum);
            System.out.println("SSE 연결 타임아웃 발생: " + userNum);
            heartbeatTask.cancel(true); // heartbeat 스케줄 정리
        });

        // SSE 오류 처리
        emitter.onError(e -> {
            emitters.remove(userNum);
            System.out.println("SSE 연결 오류 발생: " + userNum);
            heartbeatTask.cancel(true); // heartbeat 스케줄 정리
        });

        return emitter;
    }
    public void shutdownScheduler() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
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
                //e.printStackTrace();
            }
        } else {
            //System.out.println("SSE 연결 없음: " + userNum);
        }
    }

    public void sendChatToUser(String userNum, ChatAlarmResponse dto){
        SseEmitter emitter = emitters.get(userNum);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(dto));
                System.out.println("알림 전송 성공: " + userNum);
            } catch (IOException e) {
                emitters.remove(userNum);
                //System.out.println("알림 전송 실패, SSE 연결 해제: " + userNum);
                //e.printStackTrace();
            }
        } else {
            //System.out.println("SSE 연결 없음: " + userNum);
        }
    }
}

