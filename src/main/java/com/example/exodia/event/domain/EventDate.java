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

    private LocalDate startDate; // 시작일 추가
    private LocalDate endDate;   // 종료일 추가

    public static EventDate fromDto(EventDateDto dto) {
        return EventDate.builder()
                .id(dto.getId())
                .eventType(dto.getEventType())
                .startDate(LocalDate.parse(dto.getStartDate()))  // String -> LocalDate 변환
                .endDate(LocalDate.parse(dto.getEndDate()))      // String -> LocalDate 변환
                .build();
    }

    public EventDateDto toDto() {
        return EventDateDto.builder()
                .id(this.id)
                .eventType(this.eventType)
                .startDate(this.startDate.toString())  // LocalDate -> String 변환
                .endDate(this.endDate.toString())      // LocalDate -> String 변환
                .build();
    }
}
