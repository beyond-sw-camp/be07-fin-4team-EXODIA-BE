package com.example.exodia.event.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventHistoryDto {
    private Long id;
    private Long eventId;
    private String eventDate;
    private String eventRange;
    private String userNum;
}
