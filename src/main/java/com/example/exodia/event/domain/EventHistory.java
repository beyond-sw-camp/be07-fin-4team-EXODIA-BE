package com.example.exodia.event.domain;

import com.example.exodia.event.dto.EventHistoryDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    private Long eventId;        // 이벤트 ID
    private LocalDate eventDate; // 이벤트 발생 날짜
    private String eventRange;   // 변경된 날짜 범위 (변경 전후 구간 등)
    private String userNum;      // 변경한 사용자 번호

    public EventHistory(Long eventId, LocalDate eventDate, String eventRange, String userNum) {
        this.eventId = eventId;
        this.eventDate = eventDate;
        this.eventRange = eventRange;
        this.userNum = userNum;
    }

    // EventHistory -> EventHistoryDto로 변환
    public EventHistoryDto toDto() {
        return EventHistoryDto.builder()
                .id(this.id)
                .eventId(this.eventId)
                .eventDate(this.eventDate.toString())
                .eventRange(this.eventRange)
                .userNum(this.userNum)
                .build();
    }
}
