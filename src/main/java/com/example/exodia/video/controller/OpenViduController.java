package com.example.exodia.video.controller;

import java.util.Map;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.openvidu.java.client.Connection;
import io.openvidu.java.client.ConnectionProperties;
import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import io.openvidu.java.client.Session;
import io.openvidu.java.client.SessionProperties;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/openvidu")
public class OpenViduController {

    @Value("${openvidu.url}")
    private String OPENVIDU_URL;

    @Value("${openvidu.secret}")
    private String OPENVIDU_SECRET;

    private OpenVidu openvidu;

    @PostConstruct
    public void init() {
        this.openvidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }

    /**
     * Initializes a new OpenVidu session.
     * @param params Optional session properties
     * @return Session ID
     */
// OpenViduController.java
    @PostMapping("/sessions")
    public ResponseEntity<String> initializeSession(@RequestBody(required = false) Map<String, Object> params) {
        try {
            if (params != null && params.containsKey("customSessionId")) {
                String customSessionId = params.get("customSessionId").toString();
                // 한글을 제거하고 허용된 문자만 남김
                customSessionId = customSessionId.replaceAll("[^a-zA-Z0-9_-]", "");
                params.put("customSessionId", customSessionId);
            }
            SessionProperties properties = SessionProperties.fromJson(params).build();
            Session session = openvidu.createSession(properties);
            return new ResponseEntity<>(session.getSessionId(), HttpStatus.OK);
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates a new connection (token) within an existing session.
     * @param sessionId The session ID
     * @param params Optional connection properties
     * @return Connection token
     */
    @PostMapping("/sessions/{sessionId}/connections")
    public ResponseEntity<String> createConnection(@PathVariable("sessionId") String sessionId,
                                                   @RequestBody(required = false) Map<String, Object> params) {
        try {
            Session session = openvidu.getActiveSession(sessionId);
            if (session == null) {
                return new ResponseEntity<>("Session not found", HttpStatus.NOT_FOUND);
            }
            ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
            Connection connection = session.createConnection(properties);
            return new ResponseEntity<>(connection.getToken(), HttpStatus.OK);
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a participant from a session.
     * @param sessionId The session ID
     * @param token The participant's token to delete
     */
    @DeleteMapping("/sessions/{sessionId}/connections/{token}")
    public ResponseEntity<Void> deleteConnection(@PathVariable("sessionId") String sessionId, @PathVariable("token") String token) {
        try {
            Session session = openvidu.getActiveSession(sessionId);
            if (session != null) {
                session.forceDisconnect(token);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
