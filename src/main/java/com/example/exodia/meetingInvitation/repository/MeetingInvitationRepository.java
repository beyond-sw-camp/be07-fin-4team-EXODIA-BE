package com.example.exodia.meetingInvitation.repository;

import com.example.exodia.meetingInvitation.domain.MeetingInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingInvitationRepository extends JpaRepository<MeetingInvitation, Long> {
    List<MeetingInvitation> findByReservationMeetId(Long reservationMeetId);
}

