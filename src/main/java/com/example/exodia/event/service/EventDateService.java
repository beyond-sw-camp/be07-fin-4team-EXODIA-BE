package com.example.exodia.event.service;

import com.example.exodia.event.domain.EventDate;
import com.example.exodia.event.repository.EventDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventDateService {

    private final EventDateRepository eventDateRepository;

    public void setEventDate(String eventType, LocalDate eventDate) {
        EventDate existingEventDate = eventDateRepository.findByEventType(eventType).orElse(new EventDate());
        existingEventDate.setEventType(eventType);
        existingEventDate.setEventDate(eventDate);
        eventDateRepository.save(existingEventDate);
    }

    public EventDate getEventDate(String eventType) {
        return eventDateRepository.findByEventType(eventType).orElseThrow(() -> new RuntimeException("Event date not found."));
    }

    public List<EventDate> getAllEventDates() {
        return eventDateRepository.findAll();
    }

}
