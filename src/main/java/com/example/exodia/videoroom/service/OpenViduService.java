package com.example.exodia.videoroom.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import java.util.Map;
@Service
public class OpenViduService {
    @Value("${openvidu.url}")
    private String OPENVIDU_URL;

    @Value("${openvidu.secret}")
    private String OPENVIDU_SECRET;

    public String createSession() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("OPENVIDUAPP", OPENVIDU_SECRET);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> requestEntity = new HttpEntity<>("{}", headers);
        String sessionUrl = OPENVIDU_URL + "/api/sessions";

        try {
            ResponseEntity<Map> response = restTemplate.exchange(sessionUrl, HttpMethod.POST, requestEntity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return (String) response.getBody().get("id");
            } else {
                throw new RuntimeException("OpenVidu 세션 생성에 실패했습니다. 상태 코드: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("OpenVidu 세션 생성 요청 중 오류 발생: " + e.getMessage(), e);
        }
    }
}