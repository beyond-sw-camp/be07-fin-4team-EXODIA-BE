package com.example.exodia.event.controller;

import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.event.domain.EventDate;
import com.example.exodia.event.dto.EventDateDto;
import com.example.exodia.event.service.EventDateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> setEventDate(@RequestBody EventDateDto eventDateDto) {
        try {
            LocalDate eventDate = LocalDate.parse(eventDateDto.getEventDate());
            String eventType = eventDateDto.getEventType();

            eventDateService.setEventDate(eventType, eventDate);

            return new ResponseEntity<>(
                    new CommonResDto(HttpStatus.OK, "Event date set successfully", null),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return new ResponseEntity<>(
                    new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to set event date: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    @GetMapping("/getDate/{eventType}")
    public ResponseEntity<EventDate> getEventDate(@PathVariable String eventType) {
        EventDate eventDate = eventDateService.getEventDate(eventType);
        return ResponseEntity.ok(eventDate);
    }
}
