package com.example.exodia.event.repository;

import com.example.exodia.event.domain.EventHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventHistoryRepository extends JpaRepository<EventHistory, Long> {
    List<EventHistory> findByEventId(Long eventId);
}
