package com.example.exodia.reservationMeetTest.service;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.meetingRoom.domain.MeetingRoom;
import com.example.exodia.meetingRoom.repository.MeetingRoomRepository;
import com.example.exodia.reservationMeeting.dto.ReservationMeetCreateDto;
import com.example.exodia.reservationMeeting.service.ReservationMeetService;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.department.domain.Department;
import com.example.exodia.department.repository.DepartmentRepository;
import com.example.exodia.position.domain.Position;
import com.example.exodia.position.repository.PositionRepository;
import com.example.exodia.user.domain.Gender;
import com.example.exodia.user.domain.Status;
import com.example.exodia.user.domain.HireType;
import com.example.exodia.user.domain.NowStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ReservationMeetServiceTest {

    @Autowired
    private ReservationMeetService reservationMeetService;

    @Autowired
    private MeetingRoomRepository meetingRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    private MeetingRoom meetingRoom;
    private User user1;
    private User user2;

    private Department department;
    private Position position;

    @BeforeEach
    public void setUp() {
        department = departmentRepository.save(new Department("개발팀"));
        position = positionRepository.save(new Position("개발자"));

        meetingRoom = meetingRoomRepository.save(new MeetingRoom("회의실 1"));

        user1 = userRepository.save(new User(
                null, "userNum1", "profileImage1.png", "사용자1", Gender.M, Status.재직,
                "password", "user1@example.com", "주소1", "01012345678", DelYN.N,
                "123456-1234567", HireType.정규직, NowStatus.회의, 15, department,
                position, 0
        ));

        user2 = userRepository.save(new User(
                null, "userNum2", "profileImage2.png", "사용자2", Gender.W, Status.재직,
                "password", "user2@example.com", "주소2", "01087654321", DelYN.N,
                "765432-7654321", HireType.정규직, NowStatus.기타, 10, department,
                position, 0
        ));

        // 데이터 저장 확인 로그
        System.out.println("MeetingRoom saved: " + meetingRoom.getId());
        System.out.println("User1 saved: " + user1.getId());
        System.out.println("User2 saved: " + user2.getId());
    }

    @Test
    @Transactional
    public void testPessimisticLockingOnReservationCreation() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        int[] successfulReservations = {0};

        for (int i = 0; i < threadCount; i++) {
            int userId = (i % 2 == 0) ? user1.getId().intValue() : user2.getId().intValue();
            executorService.execute(() -> {
                try {
                    ReservationMeetCreateDto reservationMeetCreateDto = ReservationMeetCreateDto.builder()
                            .meetingRoomId(meetingRoom.getId())
                            .userId((long) userId)
                            .startTime(LocalDateTime.now().plusHours(1))
                            .endTime(LocalDateTime.now().plusHours(2))
                            .build();

                    reservationMeetService.createReservation(reservationMeetCreateDto);
                    successfulReservations[0]++;
                } catch (Exception e) {
                    System.out.println("예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        assertEquals(1, successfulReservations[0], "동시에 두 개의 예약이 성공해서는 안 됨");
    }
}

