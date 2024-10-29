package com.example.exodia.reservationMeeting.service;

import com.example.exodia.common.service.KafkaProducer;
import com.example.exodia.meetingInvitation.domain.MeetingInvitation;
import com.example.exodia.meetingInvitation.dto.MeetingInvitationDto;
import com.example.exodia.meetingInvitation.repository.MeetingInvitationRepository;
import com.example.exodia.meetingRoom.domain.MeetingRoom;
import com.example.exodia.meetingRoom.repository.MeetingRoomRepository;
import com.example.exodia.notification.service.NotificationService;
import com.example.exodia.reservationMeeting.domain.ReservationMeet;
import com.example.exodia.reservationMeeting.dto.ReservationMeetCreateDto;
import com.example.exodia.reservationMeeting.dto.ReservationMeetListDto;
import com.example.exodia.reservationMeeting.repository.ReservationMeetRepository;
import com.example.exodia.user.domain.NowStatus;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.UserService;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
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
    @Autowired
    private final NotificationService notificationService;
    @Autowired
    private final KafkaProducer kafkaProducer;
    @Autowired
    private final MeetingInvitationRepository meetingInvitationRepository;
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;
//    @Autowired
//    @Qualifier("10")
//    private RedisTemplate<String, Object> reservationRedisTemplate;

    public ReservationMeetService(ReservationMeetRepository reservationMeetRepository, MeetingRoomRepository meetingRoomRepository, UserRepository userRepository, UserService userService, NotificationService notificationService, KafkaProducer kafkaProducer, MeetingInvitationRepository meetingInvitationRepository) {
        this.reservationMeetRepository = reservationMeetRepository;
        this.meetingRoomRepository = meetingRoomRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.kafkaProducer = kafkaProducer;
        this.meetingInvitationRepository = meetingInvitationRepository;
    }

    /* 회의실 예약 생성 */
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public ReservationMeetListDto createReservation(ReservationMeetCreateDto reservationMeetCreateDto) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        MeetingRoom meetingRoom = meetingRoomRepository.findById(reservationMeetCreateDto.getMeetingRoomId())
                .orElseThrow(() -> new IllegalArgumentException("회의실이 존재하지 않습니다."));
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));


        // 비관적 락
        List<ReservationMeet> conflictingReservations = reservationMeetRepository
                .findConflictingReservationsWithLock(meetingRoom.getId(), reservationMeetCreateDto.getStartTime(), reservationMeetCreateDto.getEndTime());

        if (!conflictingReservations.isEmpty()) {
            throw new IllegalArgumentException("해당 시간에 회의실이 이미 예약되어 있습니다.");
        }

//        //if (isMeetingRoomAvailable(meetingRoom.getId(), reservationMeetCreateDto.getStartTime(), reservationMeetCreateDto.getEndTime())) {
//            ReservationMeet reservationMeet = ReservationMeet.fromEntity(reservationMeetCreateDto, meetingRoom, user);
//
//
//        // 관리자를 대상으로 알림 전송
//        String message = String.format("%s님이 %s 회의실을 %s에 예약하였습니다.", user.getName(), meetingRoom.getName(), reservationMeet.getStartTime().toString());
//        notificationService.sendMeetReservationReqToAdmins(message);
//
//            return ReservationMeetListDto.fromEntity(reservationMeetRepository.save(reservationMeet));
//        //} else {
//        //    throw new IllegalArgumentException("해당 시간에 회의실이 이미 예약되어 있습니다.");
//        //}
        ReservationMeet reservationMeet = ReservationMeet.fromEntity(reservationMeetCreateDto, meetingRoom, user);
        reservationMeet = reservationMeetRepository.save(reservationMeet);

        reservationMeetRepository.flush();

//        // 관리자를 대상으로 알림 전송
//        String departmentId = user.getDepartment().getId().toString();  // 예약자 부서 ID 가져오기
//        String userName = user.getName();  // 예약자 이름 가져오기
//
//        // Kafka를 통해 회의실 예약 이벤트 전송
//        kafkaProducer.sendMeetingReservationNotification(
//                "meeting-room-reservations",
//                userName,
//                meetingRoom.getName(),
//                reservationMeet.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),  // 시작 시간
//                reservationMeet.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),    // 종료 시간
//                departmentId
//        );
        return ReservationMeetListDto.fromEntity(reservationMeet);
    }

    /* 로그인 한 유저의 예약 내역 조회 */
    @Transactional(readOnly = true)
    public List<ReservationMeetListDto> getUserReservations() {
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

        try {
            userService.checkHrAuthority(user.getDepartment().getId().toString());
        } catch (Exception e) {

        }

        return reservationMeetRepository.findAll().stream()
                .map(ReservationMeetListDto::fromEntity)
                .collect(Collectors.toList());
    }
    /* 날짜에 대한 예약 일정 조회*/
    @Transactional(readOnly = true)
    public List<ReservationMeetListDto> getReservationsByDate(LocalDate date) {
        // 해당 날짜의 시작 시간과 끝 시간 계산 (00:00부터 23:59까지)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        // 해당 날짜에 예약된 모든 회의실 예약을 조회
        List<ReservationMeet> reservations = reservationMeetRepository
                .findByStartTimeBetween(startOfDay, endOfDay);

        return reservations.stream()
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

        reservationMeetRepository.deleteById(reservationId);
        reservationMeetRepository.flush();
    }

    /* 초대 메서드 - 예약자가 유저를 초대할 수 있도록 설정 */
    @Transactional
    public void inviteUsersToMeeting(Long reservationId, List<String> invitedUserNums) {
        ReservationMeet reservationMeet = reservationMeetRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("회의 예약이 존재하지 않습니다."));

        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!reservationMeet.getUser().getUserNum().equals(userNum)) {
            throw new IllegalArgumentException("예약자만 유저를 초대할 수 있습니다.");
        }

        invitedUserNums.forEach(userNumToInvite -> {
            User invitedUser = userRepository.findByUserNum(userNumToInvite)
                    .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
            MeetingInvitation invitation = new MeetingInvitation(reservationMeet, invitedUser);
            meetingInvitationRepository.save(invitation);
        });

        scheduleMeetingStatusUpdateJob(reservationId, reservationMeet.getStartTime(), reservationMeet.getEndTime());
    }

    /* 스케줄링을 통한 상태 변경 */
    private void scheduleMeetingStatusUpdateJob(Long reservationId, LocalDateTime startTime, LocalDateTime endTime) {
        taskScheduler.schedule(() -> updateMeetingStatus(reservationId, NowStatus.회의),
                Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()));

        taskScheduler.schedule(() -> updateMeetingStatus(reservationId, NowStatus.출근),
                Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant()));
    }

    /* 상태 업데이트 메서드 */
    @Transactional
    public void updateMeetingStatus(Long reservationId, NowStatus status) {
        List<MeetingInvitation> invitations = meetingInvitationRepository.findByReservationMeetId(reservationId);
        invitations.forEach(invitation -> {
            User user = invitation.getInvitedUser();
            user.setN_status(status);
            userRepository.save(user);
        });
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
    private boolean isTimeSlotValid(LocalDateTime startTime, LocalDateTime endTime) {
        return startTime.getMinute() % 30 == 0 && endTime.getMinute() % 30 == 0 && endTime.isAfter(startTime);
    }
}


