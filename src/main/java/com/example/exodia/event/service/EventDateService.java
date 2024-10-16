package com.example.exodia.event.service;

import com.example.exodia.event.domain.EventDate;
import com.example.exodia.event.domain.EventHistory;
import com.example.exodia.event.dto.EventHistoryDto;
import com.example.exodia.event.repository.EventDateRepository;
import com.example.exodia.event.repository.EventHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventDateService {

    private final EventDateRepository eventDateRepository;
    private final EventHistoryRepository eventHistoryRepository;

    public void setEventDate(String eventType, LocalDate startDate, LocalDate endDate, String userNum) {
        EventDate existingEventDate = eventDateRepository.findByEventType(eventType)
                .orElse(new EventDate());

        existingEventDate.setEventType(eventType);
        existingEventDate.setEventDate(startDate); // 주된 날짜를 startDate로 설정

        eventDateRepository.save(existingEventDate);

        EventHistory history = new EventHistory(
                existingEventDate.getId(), startDate, endDate, startDate.toString() + " ~ " + endDate.toString(), userNum
        );
        eventHistoryRepository.save(history);
    }

    public List<EventHistoryDto> getEventHistory(Long eventId) {
        List<EventHistory> eventHistories = eventHistoryRepository.findByEventId(eventId);
        return eventHistories.stream()
                .map(EventHistory::toDto)
                .collect(Collectors.toList());
    }

    public EventDate getEventDate(String eventType) {
        return eventDateRepository.findByEventType(eventType)
                .orElseThrow(() -> new RuntimeException("Event date not found for eventType: " + eventType));
    }

    public List<EventDate> getAllEvents() {
            return eventDateRepository.findAll();
    }
}

