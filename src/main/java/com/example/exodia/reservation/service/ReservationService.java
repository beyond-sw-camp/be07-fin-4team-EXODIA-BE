package com.example.exodia.reservation.service;

import com.example.exodia.car.domain.Car;
import com.example.exodia.car.repository.CarRepository;
import com.example.exodia.common.auth.JwtTokenProvider;
import com.example.exodia.reservation.domain.Reservation;
import com.example.exodia.reservation.dto.ReservationCreateDto;
import com.example.exodia.reservation.dto.ReservationDto;
import com.example.exodia.reservation.repository.ReservationRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedissonClient redissonClient; // Redisson 클라이언트 추가

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // 차량 예약 메서드
    public ReservationDto carReservation(ReservationCreateDto dto) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        RLock lock = redissonClient.getLock("carReservationLock:" + dto.getCarId() + ":" + dto.getStartDate());

        try {
            // 5초 동안 락을 기다리고, 10초 동안 락을 유지
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                Car car = carRepository.findById(dto.getCarId())
                        .orElseThrow(() -> new IllegalArgumentException("차량이 존재하지 않습니다."));
                User user = userRepository.findByUserNum(userNum)
                        .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

                // 예약 가능 여부 확인
                if (!isCarAvailableForDate(car.getId(), dto.getStartDate())) {
                    throw new IllegalArgumentException("해당 날짜에 차량이 이미 예약되어 있습니다.");
                }

                // 예약 생성 및 저장
                Reservation reservation = dto.toEntity(car, user);
                Reservation savedReservation = reservationRepository.save(reservation);

                return ReservationDto.fromEntity(savedReservation);
            } else {
                // 락을 얻지 못했을 때의 처리 (동시에 다른 사용자가 접근 중)
                throw new IllegalArgumentException("현재 다른 사용자가 예약을 진행 중입니다. 잠시 후 다시 시도해주세요.");
            }
        } catch (InterruptedException e) {
            // 예외 처리
            Thread.currentThread().interrupt();
            throw new RuntimeException("예약 중 문제가 발생했습니다. 다시 시도해주세요.");
        } finally {
            // 락이 획득되었을 경우에만 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 특정 날짜에 차량이 예약 가능한지 확인 메서드
    public boolean isCarAvailableForDate(Long carId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<Reservation> reservations = reservationRepository.findByCarIdAndDate(carId, startOfDay, endOfDay);
        return reservations.isEmpty();
    }

    // 특정 날짜의 예약 조회 메서드
    public List<ReservationDto> getReservationsForDay(LocalDateTime date) {
        return reservationRepository.findByStartTimeBetween(date, date.plusDays(1))
                .stream()
                .map(ReservationDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 모든 예약 조회 메서드
    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationDto::fromEntity)
                .collect(Collectors.toList());
    }
}


