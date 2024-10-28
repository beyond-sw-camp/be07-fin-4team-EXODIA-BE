package com.example.exodia.reservationMeeting.dto;

import com.example.exodia.reservationMeeting.domain.ReservationMeet;
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
public class ReservationMeetListDto {
    private Long id;
    private Long meetingRoomId;
    private Long userId;
    private String userProfileImage;
    private String userName;
    private String userNum;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Status status;

    public static ReservationMeetListDto fromEntity(ReservationMeet reservationMeet) {
        return ReservationMeetListDto.builder()
                .id(reservationMeet.getId())
                .meetingRoomId(reservationMeet.getMeetingRoom().getId())
                .userId(reservationMeet.getUser().getId())
                .userName(reservationMeet.getUser().getName())
                .userNum(reservationMeet.getUser().getUserNum())
                .userProfileImage(reservationMeet.getUser().getProfileImage())
                .startTime(reservationMeet.getStartTime())
                .endTime(reservationMeet.getEndTime())
                .status(reservationMeet.getStatus())
                .build();
    }
}

