package com.example.exodia.videoroom.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/sessions")
public class OpenViduController {

    @Value("${openvidu.url}")
    private String OPENVIDU_URL;

    @Value("${openvidu.secret}")
    private String OPENVIDU_SECRET;

    @PostMapping("/get-token")
    public ResponseEntity<String> createToken(@RequestBody Map<String, String> sessionInfo) {
        String sessionId = sessionInfo.get("sessionId");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("OPENVIDUAPP", OPENVIDU_SECRET);
        headers.set("Content-Type", "application/json");

        try {
            String sessionUrl = OPENVIDU_URL + "/openvidu/api/sessions/" + sessionId;
            ResponseEntity<Map> existingSessionResponse = restTemplate.exchange(sessionUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            String createdSessionId = existingSessionResponse.getStatusCode() == HttpStatus.OK
                    ? sessionId
                    : createNewSession(headers);

            Map<String, Object> tokenBody = new HashMap<>();
            tokenBody.put("session", createdSessionId);
            HttpEntity<Map<String, Object>> tokenRequest = new HttpEntity<>(tokenBody, headers);
            String tokenUrl = OPENVIDU_URL + "/openvidu/api/tokens";
            ResponseEntity<Map> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, tokenRequest, Map.class);

            return ResponseEntity.ok(tokenResponse.getBody().get("token").toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("토큰 생성 실패: " + e.getMessage());
        }
    }

    private String createNewSession(HttpHeaders headers) {
        RestTemplate restTemplate = new RestTemplate();
        String sessionUrl = OPENVIDU_URL + "/openvidu/api/sessions";
        ResponseEntity<Map> sessionResponse = restTemplate.exchange(sessionUrl, HttpMethod.POST, new HttpEntity<>("{}", headers), Map.class);
        return sessionResponse.getBody().get("id").toString();
    }


}
