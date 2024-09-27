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

import java.util.List;

@RestController
@RequestMapping("/reservation/meet")
public class ReservationMeetController {

    private final ReservationMeetService reservationMeetService;

    public ReservationMeetController(ReservationMeetService reservationMeetService) {
        this.reservationMeetService = reservationMeetService;
    }

    @PostMapping("/create")
    public ResponseEntity<CommonResDto> createReservation(@RequestBody ReservationMeetCreateDto reservationMeetCreateDto) {
        try {
            ReservationMeetListDto createdReservation = reservationMeetService.createReservation(reservationMeetCreateDto);
            return new ResponseEntity<>(new CommonResDto(HttpStatus.CREATED, "회의실 예약이 완료되었습니다.", createdReservation), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new CommonResDto(HttpStatus.CONFLICT, e.getMessage(), null), HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReservationMeetListDto>> getAllReservations() {
        List<ReservationMeetListDto> reservations = reservationMeetService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ReservationMeetListDto>> getReservationsForRoom(@PathVariable Long roomId) {
        List<ReservationMeetListDto> reservations = reservationMeetService.getReservationsForRoom(roomId);
        return ResponseEntity.ok(reservations);
    }

    @DeleteMapping("/cancel/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommonResDto> cancelReservation(@PathVariable Long id) {
        reservationMeetService.cancelReservation(id);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "예약이 취소되었습니다.", null), HttpStatus.OK);
    }
}