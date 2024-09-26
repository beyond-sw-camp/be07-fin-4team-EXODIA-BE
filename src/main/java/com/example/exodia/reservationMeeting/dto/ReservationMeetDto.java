package com.example.exodia.reservationMeeting.dto;

import com.example.exodia.meetingRoom.domain.MeetingRoom;
import com.example.exodia.reservationMeeting.domain.ReservationMeet;
import com.example.exodia.reservationMeeting.domain.Status;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationMeetDto {

    private Long meetingRoomId;
    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Status status;

    public ReservationMeet toEntity(MeetingRoom meetingRoom, User user) {
        return ReservationMeet.builder()
                .meetingRoom(meetingRoom)
                .user(user)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .status(this.status)
                .build();
    }
}

//    public static ReservationMeetDto fromEntity(ReservationMeet reservationMeet) {
//        return ReservationMeetDto.builder()
//                .meetingRoomId(reservationMeet.getMeetingRoom().getId())
//                .userId(reservationMeet.getUser().getId())
//                .startTime(reservationMeet.getStartTime())
//                .endTime(reservationMeet.getEndTime())
//                .status(reservationMeet.getStatus())
//                .build();
//    }
//}

