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
        // 필요한 Department와 Position 객체를 먼저 생성
        department = departmentRepository.save(new Department("개발팀"));
        position = positionRepository.save(new Position("개발자"));

        // MeetingRoom 객체 생성
        meetingRoom = meetingRoomRepository.save(new MeetingRoom("회의실 1"));

        // User 객체 생성 (Department와 Position 정보를 올바르게 설정)
        user1 = userRepository.save(new User(
                null,                   // ID (자동 생성)
                "userNum1",             // 사용자 번호
                "profileImage1.png",    // 프로필 이미지
                "사용자1",               // 이름
                Gender.M,            // 성별
                Status.재직,          // 상태
                "password",             // 비밀번호
                "user1@example.com",    // 이메일
                "주소1",                 // 주소
                "01012345678",          // 전화번호
                DelYN.N,                // 삭제 여부
                "123456-1234567",       // 주민등록번호
                HireType.정규직,      // 고용 유형
                NowStatus.회의,      // 현재 상태
                15,                     // 연차
                department,             // 부서
                position,               // 직책
                0                       // 로그인 실패 횟수
        ));

        user2 = userRepository.save(new User(
                null,                   // ID (자동 생성)
                "userNum2",             // 사용자 번호
                "profileImage2.png",    // 프로필 이미지
                "사용자2",               // 이름
                Gender.W,          // 성별
                Status.재직,          // 상태
                "password",             // 비밀번호
                "user2@example.com",    // 이메일
                "주소2",                 // 주소
                "01087654321",          // 전화번호
                DelYN.N,                // 삭제 여부
                "765432-7654321",       // 주민등록번호
                HireType.정규직,      // 고용 유형
                NowStatus.기타,      // 현재 상태
                10,                     // 연차
                department,             // 부서
                position,               // 직책
                0                       // 로그인 실패 횟수
        ));
    }

    @Test
    @Transactional
    public void testPessimisticLockingOnReservationCreation() throws InterruptedException {
        // 스레드 풀 설정 (동시 접속자 수)
        int threadCount = 2; // 동시 접근 시도할 스레드 수
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 동시성 테스트에서 성공한 예약 횟수 카운트
        int[] successfulReservations = {0};

        for (int i = 0; i < threadCount; i++) {
            int userId = (i % 2 == 0) ? user1.getId().intValue() : user2.getId().intValue(); // 사용자 1과 2를 번갈아 가며 사용
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

        latch.await(); // 모든 스레드가 종료될 때까지 대기

        executorService.shutdown();

        // 성공한 예약이 1개여야 함. (동시에 두 개의 예약이 성공할 수 없음)
        assertEquals(1, successfulReservations[0], "동시에 두 개의 예약이 성공해서는 안 됨");
    }
}
