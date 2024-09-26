package com.example.exodia.meetingRoom.dto;

import com.example.exodia.meetingRoom.domain.MeetingRoom;
import com.example.exodia.reservationVehicle.domain.Reservation;
import com.example.exodia.reservationVehicle.dto.ReservationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRoomUpdateDto {
    private String newName;
}

