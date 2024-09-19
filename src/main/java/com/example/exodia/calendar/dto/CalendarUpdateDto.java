package com.example.exodia.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarUpdateDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String type;
}
