package com.example.exodia.meetingInvitation.domain;

import com.example.exodia.reservationMeeting.domain.ReservationMeet;
import com.example.exodia.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_meet_id", nullable = false)
    private ReservationMeet reservationMeet;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User invitedUser;

    // 새 생성자 추가
    public MeetingInvitation(ReservationMeet reservationMeet, User invitedUser) {
        this.reservationMeet = reservationMeet;
        this.invitedUser = invitedUser;
    }
}
