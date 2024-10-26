package com.example.exodia.video.service;

import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.Session;
import io.openvidu.java.client.SessionProperties;
import io.openvidu.java.client.ConnectionProperties;
import io.openvidu.java.client.ConnectionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenViduService {
    private OpenVidu openVidu;

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

        return session.createConnection(connectionProperties).getToken();
    }
}
