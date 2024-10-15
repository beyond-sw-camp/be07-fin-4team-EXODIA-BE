package com.example.exodia.event.domain;

import com.example.exodia.event.dto.EventDateDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventType;
    private LocalDate eventDate;

    public static EventDate fromDto(EventDateDto dto) {
        return EventDate.builder()
                .id(dto.getId())
                .eventType(dto.getEventType())
                .eventDate(LocalDate.parse(dto.getEventDate()))  // String -> LocalDate 변환
                .build();
    }

    public EventDateDto toDto() {
        return EventDateDto.builder()
                .id(this.id)
                .eventType(this.eventType)
                .eventDate(this.eventDate.toString())  // LocalDate -> String 변환
                .build();
    }
}
