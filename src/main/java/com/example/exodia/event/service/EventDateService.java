package com.example.exodia.event.service;

import com.example.exodia.calendar.service.CalendarService;
import com.example.exodia.common.service.KafkaProducer;
import com.example.exodia.event.domain.EventDate;
import com.example.exodia.event.domain.EventHistory;
import com.example.exodia.event.dto.EventHistoryDto;
import com.example.exodia.event.repository.EventDateRepository;
import com.example.exodia.event.repository.EventHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventDateService {

    private final EventDateRepository eventDateRepository;
    private final EventHistoryRepository eventHistoryRepository;
    private final CalendarService calendarService;
    private final KafkaProducer kafkaProducer;

    @Transactional
    public void setEventDate(String eventType, LocalDate startDate, LocalDate endDate, String userNum) {
        EventDate existingEventDate = eventDateRepository.findByEventType(eventType)
                .orElse(new EventDate());

        existingEventDate.setEventType(eventType);
        existingEventDate.setStartDate(startDate);
        existingEventDate.setEndDate(endDate);

        eventDateRepository.save(existingEventDate);

        EventHistory history = new EventHistory(
                existingEventDate.getId(),
                startDate, endDate,
                LocalDate.now().toString(),
                userNum
        );

        eventHistoryRepository.save(history);

        // 인사평가 알림 전송 조건 추가
        if ("인사평가".equals(eventType)) {
            String message = startDate + " ~ " + endDate + " 는 인사평가 기간입니다.";
            kafkaProducer.sendBoardEvent("notice-events", message); // Kafka로 메시지 전송
        }
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

    @Transactional
    public void deleteEvent(Long eventId) {
        EventDate eventDate = eventDateRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("EventDate not found"));
        try {
            calendarService.deleteCalendarByTitle(eventDate.getEventType());
        } catch (Exception e) {
            System.out.println("캘린더 이벤트를 찾을 수 없습니다.");
        }

        eventHistoryRepository.deleteByEventId(eventId);
        eventDateRepository.deleteById(eventId);
    }
}
