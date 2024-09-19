package com.example.exodia.calendar.controller;

import com.example.exodia.calendar.dto.CalendarListDto;
import com.example.exodia.calendar.dto.CalendarSaveDto;
import com.example.exodia.calendar.service.CalendarService;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CalendarController {

    @Autowired
    private final CalendarService calendarService;
    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }


    @PostMapping("/calendar")
    public ResponseEntity<String> createEvent(@RequestBody CalendarSaveDto dto) {
        try {
            String eventId = calendarService.createGoogleCalendarEvent(dto);
            return ResponseEntity.ok(eventId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // 이벤트 수정 엔드포인트
    @PutMapping("/calendar/event/{eventId}")
    public ResponseEntity<String> updateEvent(@PathVariable String eventId, @RequestBody CalendarSaveDto dto) {
        try {
            String updatedEventId = calendarService.updateGoogleCalendarEvent(eventId, dto);
            return ResponseEntity.ok(updatedEventId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // 월별 이벤트 조회 엔드포인트
    @GetMapping("/calendar/events")
    public ResponseEntity<List<Event>> getEventsByMonth(@RequestBody CalendarListDto dto) {
        try {
            List<Event> events = calendarService.getGoogleCalendarEventsByMonth(dto);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

}
