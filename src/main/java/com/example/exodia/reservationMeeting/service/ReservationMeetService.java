package com.example.exodia.reservationMeeting.service;

import com.example.exodia.meetingRoom.domain.MeetingRoom;
import com.example.exodia.meetingRoom.repository.MeetingRoomRepository;
import com.example.exodia.reservationMeeting.domain.ReservationMeet;
import com.example.exodia.reservationMeeting.dto.ReservationMeetCreateDto;
import com.example.exodia.reservationMeeting.dto.ReservationMeetDto;
import com.example.exodia.reservationMeeting.dto.ReservationMeetListDto;
import com.example.exodia.reservationMeeting.repository.ReservationMeetRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationMeetService {

    private final ReservationMeetRepository reservationMeetRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final UserRepository userRepository;

    public ReservationMeetService(ReservationMeetRepository reservationMeetRepository, MeetingRoomRepository meetingRoomRepository, UserRepository userRepository) {
        this.reservationMeetRepository = reservationMeetRepository;
        this.meetingRoomRepository = meetingRoomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReservationMeetListDto createReservation(ReservationMeetCreateDto reservationMeetCreateDto) {
        MeetingRoom meetingRoom = meetingRoomRepository.findById(reservationMeetCreateDto.getMeetingRoomId())
                .orElseThrow(() -> new IllegalArgumentException("회의실이 존재하지 않습니다."));
        User user = userRepository.findById(reservationMeetCreateDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        if (!isTimeSlotValid(reservationMeetCreateDto.getStartTime(), reservationMeetCreateDto.getEndTime())) {
            throw new IllegalArgumentException("예약 시간은 30분 단위로 설정되어야 합니다.");
        }

        if (isMeetingRoomAvailable(meetingRoom.getId(), reservationMeetCreateDto.getStartTime(), reservationMeetCreateDto.getEndTime())) {
            ReservationMeet reservationMeet = ReservationMeet.fromEntity(reservationMeetCreateDto, meetingRoom, user);
            return ReservationMeetListDto.fromEntity(reservationMeetRepository.save(reservationMeet));
        } else {
            throw new IllegalArgumentException("해당 시간에 회의실이 이미 예약되어 있습니다.");
        }
    }

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


    // 30분 단위 체크
    private boolean isTimeSlotValid(LocalDateTime startTime, LocalDateTime endTime) {
        return startTime.getMinute() % 30 == 0 && endTime.getMinute() % 30 == 0 && endTime.isAfter(startTime);
    }

    @Transactional(readOnly = true)
    public List<ReservationMeetListDto> getReservationsForRoom(Long meetingRoomId) {
        return reservationMeetRepository.findByMeetingRoomIdAndStartTimeBetween(meetingRoomId, LocalDateTime.now(), LocalDateTime.now().plusDays(1))
                .stream()
                .map(ReservationMeetListDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationMeetListDto> getAllReservations() {
        return reservationMeetRepository.findAll().stream()
                .map(ReservationMeetListDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        ReservationMeet reservationMeet = reservationMeetRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        reservationMeetRepository.delete(reservationMeet);
    }
}


