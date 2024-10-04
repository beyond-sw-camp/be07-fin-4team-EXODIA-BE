package com.example.exodia.reservationMeeting.repository;

import com.example.exodia.meetingRoom.domain.MeetingRoom;
import com.example.exodia.reservationMeeting.domain.ReservationMeet;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationMeetRepository extends JpaRepository<ReservationMeet, Long> {
    List<ReservationMeet> findByMeetingRoomIdAndStartTimeBetween(Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime);

    // (비관적 락) 중복 예약을 검사하는 쿼리
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReservationMeet r WHERE r.meetingRoom.id = :roomId AND r.startTime < :endTime AND r.endTime > :startTime")
    List<ReservationMeet> findConflictingReservationsWithLock(@Param("roomId") Long roomId,
                                                              @Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime);
    List<ReservationMeet> findByUserId(Long userId);
    List<ReservationMeet> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

}
