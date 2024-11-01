package com.example.exodia.videoroom.service;

import io.openvidu.java.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenViduService {

    private OpenVidu openVidu;

    public OpenViduService(@Value("${openvidu.url}") String openViduUrl,
                           @Value("${openvidu.secret}") String secret) {
        this.openVidu = new OpenVidu(openViduUrl, secret);
    }

    // 세션 생성
    public String createSession() throws OpenViduJavaClientException, OpenViduHttpException {
        SessionProperties properties = new SessionProperties.Builder().build();
        Session session = openVidu.createSession(properties);
        return session.getSessionId();
    }

    // 연결 생성
    public String createConnection(String sessionId) throws OpenViduJavaClientException, OpenViduHttpException {
        Session session = openVidu.getActiveSession(sessionId);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }
        Connection connection = session.createConnection(new ConnectionProperties.Builder().build());
        return connection.getToken();
    }

    // 세션 종료
    public void closeSession(String sessionId) throws OpenViduJavaClientException, OpenViduHttpException {
        Session session = openVidu.getActiveSession(sessionId);
        if (session != null) {
            session.close();
        }
    }
}
