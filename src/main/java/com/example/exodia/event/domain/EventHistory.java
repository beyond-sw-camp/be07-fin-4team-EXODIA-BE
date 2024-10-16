package com.example.exodia.event.domain;

import com.example.exodia.event.dto.EventHistoryDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long eventId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String eventRange; // 실제 변경된 날짜
    private String userNum;

    public EventHistory(Long eventId, LocalDate startDate, LocalDate endDate, String eventRange, String userNum) {
        this.eventId = eventId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.eventRange = eventRange;
        this.userNum = userNum;
    }

    public EventHistoryDto toDto() {
        return EventHistoryDto.builder()
                .id(this.id)
                .eventId(this.eventId)
                .startDate(this.startDate.toString())
                .endDate(this.endDate.toString())
                .eventRange(this.eventRange)
                .userNum(this.userNum)
                .build();
    }
}
