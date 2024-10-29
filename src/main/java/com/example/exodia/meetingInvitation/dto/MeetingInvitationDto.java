package com.example.exodia.meetingInvitation.dto;

import com.example.exodia.meetingInvitation.domain.MeetingInvitation;
import com.example.exodia.reservationMeeting.domain.ReservationMeet;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingInvitationDto {
    private Long reservationMeetId;
    private String invitedUserNum;

    public MeetingInvitation toEntity(ReservationMeet reservationMeet, User invitedUser) {
        return MeetingInvitation.builder()
                .reservationMeet(reservationMeet)
                .invitedUser(invitedUser)
                .build();
    }
}