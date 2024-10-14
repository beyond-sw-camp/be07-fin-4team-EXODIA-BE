package com.example.exodia.event.repository;

import com.example.exodia.event.domain.EventDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventDateRepository extends JpaRepository<EventDate, Long> {
    Optional<EventDate> findByEventType(String eventType);
}
