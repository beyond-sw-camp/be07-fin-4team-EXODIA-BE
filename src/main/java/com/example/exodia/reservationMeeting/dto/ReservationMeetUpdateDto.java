package com.example.exodia.reservationMeeting.dto;

import com.example.exodia.reservationMeeting.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationMeetUpdateDto {
    private Long reservationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Status status;
}