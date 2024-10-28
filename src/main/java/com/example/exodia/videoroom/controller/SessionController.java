package com.example.exodia.videoroom.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final String OPENVIDU_URL = "http://localhost:4443";
    private final String OPENVIDU_SECRET = "MY_SECRET";

    @PostMapping("/get-token")
    public ResponseEntity<String> getToken(@RequestBody Map<String, String> sessionInfo) {
        RestTemplate restTemplate = new RestTemplate();
        String sessionId = sessionInfo.get("sessionId");

        // 1. 세션 생성 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("OPENVIDUAPP", OPENVIDU_SECRET);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> sessionRequest = new HttpEntity<>("{}", headers);
        String sessionUrl = OPENVIDU_URL + "/api/sessions";

        ResponseEntity<Map> sessionResponse = restTemplate.exchange(
                sessionUrl, HttpMethod.POST, sessionRequest, Map.class
        );

        String createdSessionId = (String) sessionResponse.getBody().get("id");

        // 2. 토큰 생성 요청
        Map<String, Object> tokenBody = new HashMap<>();
        tokenBody.put("session", createdSessionId);
        HttpEntity<Map<String, Object>> tokenRequest = new HttpEntity<>(tokenBody, headers);
        String tokenUrl = OPENVIDU_URL + "/api/tokens";

        ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                tokenUrl, HttpMethod.POST, tokenRequest, Map.class
        );

        if (tokenResponse.getStatusCode() == HttpStatus.OK) {
            String token = (String) tokenResponse.getBody().get("token");
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("토큰 생성에 실패했습니다.");
        }
    }
}
