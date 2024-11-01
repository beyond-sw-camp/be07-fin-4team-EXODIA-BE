package com.example.exodia.reservationVehicle.service;

import com.example.exodia.car.domain.Car;
import com.example.exodia.car.repository.CarRepository;
import com.example.exodia.common.auth.JwtTokenProvider;
import com.example.exodia.common.service.KafkaProducer;
import com.example.exodia.notification.service.NotificationService;
import com.example.exodia.reservationVehicle.domain.Reservation;
import com.example.exodia.reservationVehicle.domain.Status;
import com.example.exodia.reservationVehicle.dto.CarReservationStatusDto;
import com.example.exodia.reservationVehicle.dto.ReservationCreateDto;
import com.example.exodia.reservationVehicle.dto.ReservationDto;
import com.example.exodia.reservationVehicle.repository.ReservationRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.UserService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
//    @Autowired
//    private RedissonClient redissonClient; // Redisson 클라이언트 추가
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private KafkaProducer kafkaProducer;


    // 차량 예약 메서드
//    @Transactional
//    public ReservationDto carReservation(ReservationCreateDto dto) {
//        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
////        RLock lock = redissonClient.getLock("carReservationLock:" + dto.getCarId() + ":" + dto.getStartDate());
//
//        try {
//            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
//                Car car = carRepository.findById(dto.getCarId())
//                        .orElseThrow(() -> new IllegalArgumentException("차량이 존재하지 않습니다."));
//                User user = userRepository.findByUserNum(userNum)
//                        .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
//
//                // 예약 기간 내에 다른 예약이 있는지 확인
//                List<Reservation> existingReservations = reservationRepository.findByCarIdAndDateRange(
//                        car.getId(),
//                        dto.getStartDate().atStartOfDay(),
//                        dto.getEndDate().atTime(LocalTime.MAX));
//
//                boolean canReserve = existingReservations.stream().allMatch(Reservation::canReserve);
//                if (!canReserve) {
//                    throw new IllegalArgumentException("해당 기간에 차량이 이미 예약되어 있습니다.");
//                }
//
//                Reservation reservation = dto.toEntity(car, user);
//                reservation.setStatus(Status.WAITING);
//                Reservation savedReservation = reservationRepository.save(reservation);
//                kafkaProducer.sendCarReservationNotification(
//                        "car-reservation-events",
//                        user.getUserNum(),
//                        user.getName(), // 유저명
//                        car.getCarNum(), // 차량 번호
//                        dto.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), //예약일
//                        dto.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) // 예약종료일
//                );
//
//                return ReservationDto.fromEntity(savedReservation);
//            } else {
//                throw new IllegalArgumentException("다른 사용자가 예약을 진행 중입니다. 잠시 후 다시 시도해주세요.");
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException("예약 중 문제가 발생했습니다. 다시 시도해주세요.");
//        } finally {
//            if (lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
//        }
//    }
    @Transactional
    public ReservationDto approveReservation(Long reservationId) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        userService.checkHrAuthority(user.getDepartment().getId().toString());
        reservation.approveReservation();
        reservation.reserve(); // 최종 예약 확정 상태로 변경
        Reservation updatedReservation = reservationRepository.save(reservation);

        // 사용자에게 알림 전송 (예약 승인)
        kafkaProducer.sendReservationApprovalNotification(
                "car-reservation-approval-events",
                reservation.getUser().getUserNum(), // 사용자 번호 추가
                reservation.getCar().getCarNum(),   // 차량 번호 추가
                reservation.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                reservation.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );
        return ReservationDto.fromEntity(updatedReservation);
    }

    @Transactional
    /* 예약 거절 */
    public void rejectReservation(Long reservationId) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        userService.checkHrAuthority(user.getDepartment().getId().toString());

        // 사용자에게 알림 전송 (예약 거절)
        kafkaProducer.sendReservationRejectionNotification(
                "car-reservation-approval-events",
                reservation.getUser().getUserNum(), // 사용자 번호 추가
                reservation.getCar().getCarNum(),
                reservation.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                reservation.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );

        // 예약 삭제
        reservationRepository.delete(reservation);
    }

    @Transactional
    // 특정 날짜에 차량이 예약 가능한지 확인 메서드
    public boolean isCarAvailableForDate(Long carId, LocalDate date) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<Reservation> reservations = reservationRepository.findByCarIdAndDate(carId, startOfDay, endOfDay);

        // 예약이 없거나 모든 예약이 취소된 경우 예약 가능
        return reservations.stream().allMatch(reservation -> reservation.getStatus() == Status.REJECTED);
    }

    // 특정 날짜의 예약 조회 메서드
    public List<ReservationDto> getReservationsForDay(LocalDateTime date) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return reservationRepository.findByStartTimeBetween(date, date.plusDays(1))
                .stream().map(ReservationDto::fromEntity).collect(Collectors.toList());
    }
    @Transactional
    // 모든 예약 조회 메서드
    public List<ReservationDto> getAllReservations() {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

//        userService.checkHrAuthority(user.getDepartment().getId().toString());

        return reservationRepository.findAll().stream()
                .map(ReservationDto::fromEntity)
                .collect(Collectors.toList());
    }
    @Transactional
    public List<CarReservationStatusDto> getAllCarsWithReservationStatusForDay(LocalDateTime date) {
        List<Car> cars = carRepository.findAll();
        List<CarReservationStatusDto> carReservationStatusList = new ArrayList<>();

        for (Car car : cars) {
            List<Reservation> reservations = reservationRepository.findByCarIdAndDate(
                    car.getId(),
                    date,
                    date.plusDays(1).minusSeconds(1)
            );

            // 예약 상태 확인: 예약이 없으면 AVAILABLE, 있으면 RESERVED
            boolean isAvailable = reservations.isEmpty();
            User user = isAvailable ? null : reservations.get(0).getUser(); // 예약된 경우 사용자 정보 가져오기

            // Create DTO using fromEntity method
            CarReservationStatusDto carReservationStatusDto = CarReservationStatusDto.fromEntity(
                    car,
                    isAvailable ? Status.AVAILABLE : Status.RESERVED,
                    user
            );

            carReservationStatusList.add(carReservationStatusDto);
        }

        return carReservationStatusList;
    }

}


