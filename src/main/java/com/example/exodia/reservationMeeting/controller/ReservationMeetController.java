package com.example.exodia.reservationMeeting.controller;

import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.reservationMeeting.dto.ReservationMeetCreateDto;
import com.example.exodia.reservationMeeting.dto.ReservationMeetDto;
import com.example.exodia.reservationMeeting.dto.ReservationMeetListDto;
import com.example.exodia.reservationMeeting.service.ReservationMeetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/reservation/meet")
public class ReservationMeetController {

    private final ReservationMeetService reservationMeetService;

    public ReservationMeetController(ReservationMeetService reservationMeetService) {
        this.reservationMeetService = reservationMeetService;
    }

    /* 예약 생성 */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/create")
    //@Operation(summary= "[일반 사용자] 회의실 예약 생성 API")
    public ResponseEntity<CommonResDto> createReservation(@RequestBody ReservationMeetCreateDto reservationMeetCreateDto) {
        try {
            ReservationMeetListDto createdReservation = reservationMeetService.createReservation(reservationMeetCreateDto);
            return new ResponseEntity<>(new CommonResDto(HttpStatus.CREATED, "회의실 예약이 완료되었습니다.", createdReservation), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new CommonResDto(HttpStatus.CONFLICT, e.getMessage(), null), HttpStatus.CONFLICT);
        }
    }

    /* 유저 각자의 예약 내역 조회 */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my-reservations")
    //@Operation(summary= "[유저 사용자] 회의실 예약 조회 API")
    public ResponseEntity<List<ReservationMeetListDto>> getUserReservations() {
        List<ReservationMeetListDto> reservations = reservationMeetService.getUserReservations();
        return ResponseEntity.ok(reservations);
    }

    /* 모든 예약 내역 조회 (관리자 권한) */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    //@Operation(summary= "[관리자 사용자] 회의실 예약 조회 API")
    public ResponseEntity<List<ReservationMeetListDto>> getAllReservations() {
        List<ReservationMeetListDto> reservations = reservationMeetService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }
    /* 자신이 생성한 예약 내역 조회 (유저 개인)*/


    /* 해당 일자 + 해당 방의 일 가능 예약 시간대 조회 */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/available-times")
    //@Operation(summary= "[일반 사용자] 회의실 예약 시간 조회 API")
    public ResponseEntity<List<LocalTime>> getAvailableTimeSlots(
            @RequestParam("meetingRoomId") Long meetingRoomId,
            @RequestParam("date") String date) {

        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime dateTime = localDate.atStartOfDay();

        List<LocalTime> availableTimeSlots = reservationMeetService.getAvailableTimeSlots(meetingRoomId, dateTime);
        return ResponseEntity.ok(availableTimeSlots);
    }

    /* 예약 취소 */
    /* 글을 작성한 유저랑 & 관리자가 삭제 가능 */
    @DeleteMapping("/cancel/{id}")
    //@Operation(summary= "[일반 사용자 + 관리자] 회의실 예약 이벤트 생성 API")
    public ResponseEntity<CommonResDto> cancelReservation(@PathVariable Long id) {
        reservationMeetService.cancelReservation(id);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "예약이 취소되었습니다.", id), HttpStatus.OK);
    }
}
//    @GetMapping("/room/{roomId}")
//    public ResponseEntity<List<ReservationMeetListDto>> getReservationsForRoom(@PathVariable Long roomId) {
//        List<ReservationMeetListDto> reservations = reservationMeetService.getReservationsForRoom(roomId);
//        return ResponseEntity.ok(reservations);
//    }