package com.example.exodia.reservationMeeting.service;

import com.example.exodia.meetingRoom.domain.MeetingRoom;
import com.example.exodia.meetingRoom.repository.MeetingRoomRepository;
import com.example.exodia.reservationMeeting.domain.ReservationMeet;
import com.example.exodia.reservationMeeting.domain.Status;
import com.example.exodia.reservationMeeting.dto.ReservationMeetCreateDto;
import com.example.exodia.reservationMeeting.dto.ReservationMeetDto;
import com.example.exodia.reservationMeeting.dto.ReservationMeetListDto;
import com.example.exodia.reservationMeeting.repository.ReservationMeetRepository;
import com.example.exodia.reservationVehicle.domain.Reservation;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.UserService;
import jakarta.persistence.LockModeType;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationMeetService {

    @Autowired
    private final ReservationMeetRepository reservationMeetRepository;
    @Autowired
    private final MeetingRoomRepository meetingRoomRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final UserService userService;

//    @Autowired
//    @Qualifier("10")
//    private RedisTemplate<String, Object> reservationRedisTemplate;

    public ReservationMeetService(ReservationMeetRepository reservationMeetRepository, MeetingRoomRepository meetingRoomRepository, UserRepository userRepository, UserService userService) {
        this.reservationMeetRepository = reservationMeetRepository;
        this.meetingRoomRepository = meetingRoomRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /* 회의실 예약 생성 */
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public ReservationMeetListDto createReservation(ReservationMeetCreateDto reservationMeetCreateDto) {

        MeetingRoom meetingRoom = meetingRoomRepository.findById(reservationMeetCreateDto.getMeetingRoomId())
                .orElseThrow(() -> new IllegalArgumentException("회의실이 존재하지 않습니다."));

        User user = userRepository.findById(reservationMeetCreateDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        // 비관적 락
        List<ReservationMeet> conflictingReservations = reservationMeetRepository
                .findConflictingReservationsWithLock(meetingRoom.getId(), reservationMeetCreateDto.getStartTime(), reservationMeetCreateDto.getEndTime());

        if (!conflictingReservations.isEmpty()) {
            throw new IllegalArgumentException("해당 시간에 회의실이 이미 예약되어 있습니다.");
        }

        //if (isMeetingRoomAvailable(meetingRoom.getId(), reservationMeetCreateDto.getStartTime(), reservationMeetCreateDto.getEndTime())) {
            ReservationMeet reservationMeet = ReservationMeet.fromEntity(reservationMeetCreateDto, meetingRoom, user);
            return ReservationMeetListDto.fromEntity(reservationMeetRepository.save(reservationMeet));
        //} else {
        //    throw new IllegalArgumentException("해당 시간에 회의실이 이미 예약되어 있습니다.");
        //}
    }

    /* 로그인 한 유저의 예약 내역 조회 */
    @Transactional(readOnly = true)
    public List<ReservationMeetListDto> getUserReservations() {
        // 현재 로그인된 사용자 정보 가져오기
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));

        List<ReservationMeet> userReservations = reservationMeetRepository.findByUserId(user.getId());

        return userReservations.stream()
                .map(ReservationMeetListDto::fromEntity)
                .collect(Collectors.toList());
    }

    /* 모든 예약 내역 조회(ADMIN) */
    @Transactional(readOnly = true)
    public List<ReservationMeetListDto> getAllReservations() {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        userService.checkHrAuthority(user.getDepartment().getId().toString());

        return reservationMeetRepository.findAll().stream()
                .map(ReservationMeetListDto::fromEntity)
                .collect(Collectors.toList());
    }

    /* 입력한 일 + 해당 방의 남은 예약 시간대 조회 */
    @Transactional
    public List<LocalTime> getAvailableTimeSlots(Long meetingRoomId, LocalDateTime date) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 시작 시간과 종료 시간 설정 (09:00 ~ 18:00) -> 평균적 직장인 일하는 정규 시간
        LocalTime startOfDay = LocalTime.of(9, 0);
        LocalTime endOfDay = LocalTime.of(18, 0);

        LocalDateTime startDateTime = date.with(startOfDay);
        LocalDateTime endDateTime = date.with(endOfDay);

        List<ReservationMeet> reservations = reservationMeetRepository.findByMeetingRoomIdAndStartTimeBetween(meetingRoomId, startDateTime, endDateTime);

        List<LocalTime> availableTimeSlots = new ArrayList<>();

        LocalTime currentTime = startOfDay;
        while (currentTime.isBefore(endOfDay)) {
            availableTimeSlots.add(currentTime);
            currentTime = currentTime.plusMinutes(30);
        }

        for (ReservationMeet reservation : reservations) {
            LocalTime reservationStartTime = reservation.getStartTime().toLocalTime();
            LocalTime reservationEndTime = reservation.getEndTime().toLocalTime();
            LocalTime slotTime = reservationStartTime;
            while (slotTime.isBefore(reservationEndTime)) {
                availableTimeSlots.remove(slotTime);
                slotTime = slotTime.plusMinutes(30);
            }
        }

        return availableTimeSlots;
    }

    /* 예약 삭제 */
    @Transactional
    public void cancelReservation(Long reservationId) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));

        ReservationMeet reservationMeet = reservationMeetRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if (!reservationMeet.getUser().getId().equals(user.getId())) {
            try {
                userService.checkHrAuthority(user.getDepartment().getId().toString());
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("본인 또는 관리자만 예약을 취소할 수 있습니다.");
            }
        }

        reservationMeetRepository.delete(reservationMeet);
    }


    //    @Transactional
    //    public void cleanExpiredReservations() {
    //        List<ReservationMeet> expiredReservations = reservationMeetRepository.findAll()
    //                .stream()
    //                .filter(reservation -> reservation.getEndTime().isBefore(LocalDateTime.now()))
    //                .collect(Collectors.toList());
    //
    //        for (ReservationMeet reservation : expiredReservations) {
    //            reservation.setStatus(Status.AVAILABLE);
    //            reservationMeetRepository.save(reservation);
    //            System.out.println("Expired reservation cleaned: " + reservation.getId());
    //        }
    //    }

    public boolean isMeetingRoomAvailable(Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<ReservationMeet> reservations = reservationMeetRepository.findByMeetingRoomIdAndStartTimeBetween(meetingRoomId, startTime, endTime);
        for (ReservationMeet reservation : reservations) {
            // 새로운 예약의 시작 시간이 기존 예약의 시간대에 있는 경우
            if ((startTime.isAfter(reservation.getStartTime()) || startTime.isEqual(reservation.getStartTime()))
                    && startTime.isBefore(reservation.getEndTime())) {
                return false;
            }
            // 새로운 예약의 종료 시간이 기존 예약의 시간대에 있는 경우
            if (endTime.isAfter(reservation.getStartTime())
                    && (endTime.isBefore(reservation.getEndTime()) || endTime.isEqual(reservation.getEndTime()))) {
                return false;
            }
            // 기존 예약의 시간대가 새로운 예약의 시간대에 완전히 포함된 경우
            if ((startTime.isBefore(reservation.getStartTime()) || startTime.isEqual(reservation.getStartTime()))
                    && (endTime.isAfter(reservation.getEndTime()) || endTime.isEqual(reservation.getEndTime()))) {
                return false;
            }
        }
        return true;
    }
    private boolean isTimeSlotValid(LocalDateTime startTime, LocalDateTime endTime) {
        return startTime.getMinute() % 30 == 0 && endTime.getMinute() % 30 == 0 && endTime.isAfter(startTime);
    }

}


