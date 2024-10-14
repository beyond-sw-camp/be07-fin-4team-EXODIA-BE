package com.example.exodia.event.service;

import com.example.exodia.event.domain.EventDate;
import com.example.exodia.event.repository.EventDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EventDateService {

    private final EventDateRepository eventDateRepository;

    public void setEventDate(String eventType, LocalDate eventDate) {
        EventDate existingEventDate = eventDateRepository.findByEventType(eventType).orElse(null);
        if (existingEventDate != null) {
            existingEventDate.setEventDate(eventDate);
            eventDateRepository.save(existingEventDate);
        } else {
            EventDate newEventDate = new EventDate();
            newEventDate.setEventType(eventType);
            newEventDate.setEventDate(eventDate);
            eventDateRepository.save(newEventDate);
        }
    }

    public EventDate getEventDate(String eventType) {
        return eventDateRepository.findByEventType(eventType).orElse(null);
    }
}
