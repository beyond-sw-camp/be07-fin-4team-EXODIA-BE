package com.example.exodia.event.controller;

import com.example.exodia.event.domain.EventDate;
import com.example.exodia.event.dto.EventDateDto;
import com.example.exodia.event.service.EventDateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/eventDate")
@RequiredArgsConstructor
public class EventDateController {

    private final EventDateService eventDateService;

    @PostMapping("/setDate")
    public ResponseEntity<?> setEventDate(@RequestBody EventDateDto eventDateDto) {
        LocalDate eventDate = LocalDate.parse(eventDateDto.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        EventDate entity = EventDate.fromDto(eventDateDto);
        eventDateService.setEventDate(entity.getEventType(), eventDate);
        return ResponseEntity.ok("Event date set successfully");
    }

    @GetMapping("/getDate/{eventType}")
    public ResponseEntity<EventDateDto> getEventDate(@PathVariable String eventType) {
        EventDate eventDate = eventDateService.getEventDate(eventType);
        return ResponseEntity.ok(eventDate.toDto());
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<EventDateDto>> getAllEventDates() {
        List<EventDateDto> eventDateDtos = eventDateService.getAllEventDates().stream()
                .map(EventDate::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventDateDtos);
    }
}
