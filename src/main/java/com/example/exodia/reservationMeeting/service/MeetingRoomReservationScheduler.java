package com.example.exodia.reservationMeeting.service;

import com.example.exodia.reservationMeeting.domain.ReservationMeet;
import com.example.exodia.reservationMeeting.domain.Status;
import com.example.exodia.reservationMeeting.dto.ReservationMeetCreateDto;
import com.example.exodia.reservationMeeting.repository.ReservationMeetRepository;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

//@Component
//@Slf4j
//public class MeetingRoomReservationScheduler {
//
//    private final ReservationMeetService reservationMeetService;
//
//    public MeetingRoomReservationScheduler(ReservationMeetService reservationMeetService) {
//        this.reservationMeetService = reservationMeetService;
//    }
//
//
//    // 만료된 예약 상태를 변경하기 위한 정기 스케줄링 작업
//    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
//    @SchedulerLock(name = "createReservation", lockAtLeastFor = "PT1M", lockAtMostFor = "PT5M")
//    public void cleanExpiredReservations() {
//        reservationMeetService.createReservation(); // 정기적으로 만료된 예약을 정리
//    }
//}


