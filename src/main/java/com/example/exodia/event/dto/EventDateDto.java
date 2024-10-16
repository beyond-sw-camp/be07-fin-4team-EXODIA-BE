package com.example.exodia.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDateDto {
    private Long id;
    private String eventType;
    private String eventDate;
    private String startDate;
    private String endDate;
    private String userNum;
}
