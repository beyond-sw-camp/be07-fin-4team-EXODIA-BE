package com.example.exodia.reservationVehicle.dto;

import com.example.exodia.car.domain.Car;
import com.example.exodia.reservationVehicle.domain.Reservation;
import com.example.exodia.reservationVehicle.domain.Status;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationCreateDto {

    private Long carId;
    private LocalDate startDate; // 날짜만 입력 받음
    private LocalDate endDate;

    public Reservation toEntity(Car car, User user) {
        // 하루 예약 처리: 해당 날짜의 시작과 끝 시간 설정
//        LocalDateTime startTime = startDate.atStartOfDay();
//        LocalDateTime endTime = startDate.atTime(LocalTime.MAX); // 59:59:9999


        return Reservation.builder()
                .car(car)
                .user(user)
                .startTime(startDate.atStartOfDay()) // 시작 날짜의 시작 시간
                .endTime(endDate.atTime(23, 59, 59))
                .status(Status.WAITING) // AVAILABLE -> WAITING
                .build();
    }
}
