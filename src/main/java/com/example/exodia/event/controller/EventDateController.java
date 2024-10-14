package com.example.exodia.event.controller;

import com.example.exodia.event.domain.EventDate;
import com.example.exodia.event.service.EventDateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/eventDate")
@RequiredArgsConstructor
public class EventDateController {

    private final EventDateService eventDateService;

    @PostMapping("/setDate")
    public ResponseEntity<Void> setEventDate(@RequestBody Map<String, String> request) {
        String eventType = request.get("eventType");
        LocalDate eventDate = LocalDate.parse(request.get("eventDate"));
        eventDateService.setEventDate(eventType, eventDate);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getDate/{eventType}")
    public ResponseEntity<EventDate> getEventDate(@PathVariable String eventType) {
        EventDate eventDate = eventDateService.getEventDate(eventType);
        return ResponseEntity.ok(eventDate);
    }
}
