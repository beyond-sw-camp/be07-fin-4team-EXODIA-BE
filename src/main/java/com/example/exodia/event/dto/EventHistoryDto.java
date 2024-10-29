package com.example.exodia.event.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventHistoryDto {

    private Long id;
    private Long eventId;
    private String startDate;
    private String endDate;
    private String eventRange;
    private String userNum;

    @Builder
    public EventHistoryDto(Long id, Long eventId, String startDate, String endDate, String eventRange, String userNum) {
        this.id = id;
        this.eventId = eventId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.eventRange = eventRange;
        this.userNum = userNum;
    }
}
