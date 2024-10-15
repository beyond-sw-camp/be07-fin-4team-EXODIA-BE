package com.example.exodia.event.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EventDateDto {
    private Long id;
    private String eventType;
    private String eventDate;
}
