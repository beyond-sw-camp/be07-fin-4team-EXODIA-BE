package com.example.exodia.video.controller;

import com.example.exodia.video.service.OpenViduService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/openvidu")
public class OpenViduController {

    private final OpenViduService openViduService;

    public OpenViduController(OpenViduService openViduService) {
        this.openViduService = openViduService;
    }

    @PostMapping("/session")
    public String createSession() throws Exception {
        return openViduService.createSession();
    }

    @PostMapping("/token")
    public String createToken(@RequestParam String sessionId) throws Exception {
        return openViduService.generateToken(sessionId);
    }
}
