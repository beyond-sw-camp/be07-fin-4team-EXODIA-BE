package com.example.exodia.reservationMeeting.service;

import com.example.exodia.reservationMeeting.domain.ReservationMeet;
import com.example.exodia.reservationMeeting.domain.Status;
import com.example.exodia.reservationMeeting.repository.ReservationMeetRepository;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MeetingRoomReservationScheduler {

    private final ReservationMeetRepository reservationMeetRepository;

    public MeetingRoomReservationScheduler(ReservationMeetRepository reservationMeetRepository) {
        this.reservationMeetRepository = reservationMeetRepository;
    }


    // 5분마다 만료된 예약을 확인하고 상태를 업데이트하는 스케줄러
    @SchedulerLock(name = "cleanExpiredReservations", lockAtLeastFor = "PT10S", lockAtMostFor = "PT30S")
    @Scheduled(cron = "0 0/5 * * * *")
    public void cleanExpiredReservations() {
        List<ReservationMeet> expiredReservations = reservationMeetRepository.findAll()
                .stream()
                .filter(reservation -> reservation.getEndTime().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());

        for (ReservationMeet res : expiredReservations) {
            res.setStatus(Status.AVAILABLE);
            reservationMeetRepository.save(res);
            System.out.println("Expired reservation cleaned: " + res.getId());
        }
    }
}

