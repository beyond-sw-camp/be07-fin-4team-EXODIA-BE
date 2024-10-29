package com.example.exodia.video.service;

import io.openvidu.java.client.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OpenViduService {
    private OpenVidu openVidu;

    @Resource(name = "videoRoomRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    public OpenViduService(@Value("${openvidu.url}") String url, @Value("${openvidu.secret}") String secret) {
        this.openVidu = new OpenVidu(url, secret);
    }

    public String createSession() throws Exception {
        Session session = openVidu.createSession(new SessionProperties.Builder().build());
        return session.getSessionId();
    }

    public String generateToken(String sessionId) throws Exception {
        Session session = openVidu.getActiveSessions().stream()
                .filter(s -> s.getSessionId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new Exception("세션을 찾을 수 없습니다."));

        ConnectionProperties connectionProperties = new ConnectionProperties.Builder()
                .type(ConnectionType.WEBRTC)
                .build();

        String token = session.createConnection(connectionProperties).getToken();

        // Redis에 참가자 정보를 저장
        Long result = redisTemplate.opsForSet().add("session:" + sessionId + ":participants", token);
        System.out.println("Redis 저장 결과: " + (result > 0) + " / 세션 ID: " + sessionId + " / 토큰: " + token);

        // 저장 결과 로그 출력
        System.out.println("Redis 저장 결과: " + result + " / 세션 ID: " + sessionId + " / 토큰: " + token);

        return token;
    }

    public Map<String, Integer> getActiveSessions() {
        Set<String> sessionKeys = redisTemplate.keys("session:*:participants");
        Map<String, Integer> sessions = new HashMap<>();

        if (sessionKeys != null) {
            for (String key : sessionKeys) {
                String sessionId = key.split(":")[1];
                int participantCount = redisTemplate.opsForSet().size(key).intValue();
                sessions.put(sessionId, participantCount);
            }
        }

        return sessions;
    }

    public void removeParticipant(String sessionId, String token) {
        redisTemplate.opsForSet().remove("session:" + sessionId + ":participants", token);

        Long participantCount = redisTemplate.opsForSet().size("session:" + sessionId + ":participants");

        if (participantCount == null || participantCount == 0) {
            redisTemplate.delete("session:" + sessionId + ":participants");
            redisTemplate.delete("session:" + sessionId);
            System.out.println("세션 삭제됨: " + sessionId);
        } else {
            System.out.println("남아있는 참가자 수: " + participantCount);
        }
    }

}
