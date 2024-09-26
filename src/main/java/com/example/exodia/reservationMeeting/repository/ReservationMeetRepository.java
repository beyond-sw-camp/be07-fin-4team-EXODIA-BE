package com.example.exodia.reservationMeeting.repository;

import com.example.exodia.meetingRoom.domain.MeetingRoom;
import com.example.exodia.reservationMeeting.domain.ReservationMeet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationMeetRepository extends JpaRepository<ReservationMeet, Long> {
    List<ReservationMeet> findByMeetingRoomIdAndStartTimeBetween(Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime);
}
