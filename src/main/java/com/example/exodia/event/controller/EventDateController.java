package com.example.exodia.event.controller;

import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.event.domain.EventDate;
import com.example.exodia.event.dto.EventDateDto;
import com.example.exodia.event.dto.EventHistoryDto;
import com.example.exodia.event.service.EventDateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/eventDate")
@RequiredArgsConstructor
public class EventDateController {

    private final EventDateService eventDateService;
    @PostMapping("/setDate")
    public ResponseEntity<?> setEventDate(@RequestBody EventDateDto eventDateDto) {
        try {
            LocalDate startDate = LocalDate.parse(eventDateDto.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate endDate = LocalDate.parse(eventDateDto.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String eventType = eventDateDto.getEventType();
            String userNum = eventDateDto.getUserNum();

            eventDateService.setEventDate(eventType, startDate, endDate, userNum);

            return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "Event date set successfully", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to set event date: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/getEventId/{eventType}")
    public ResponseEntity<EventDate> getEventIdByType(@PathVariable String eventType) {
        EventDate eventDate = eventDateService.getEventDate(eventType);
        if (eventDate != null) {
            return ResponseEntity.ok(eventDate);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/getHistory/{eventId}")
    public ResponseEntity<List<EventHistoryDto>> getEventHistory(@PathVariable Long eventId) {
        List<EventHistoryDto> histories = eventDateService.getEventHistory(eventId);
        return ResponseEntity.ok(histories);
    }

    @GetMapping("/all")
    public ResponseEntity<List<EventDate>> getAllEvents() {
        List<EventDate> events = eventDateService.getAllEvents();
        return ResponseEntity.ok(events);
    }

}
