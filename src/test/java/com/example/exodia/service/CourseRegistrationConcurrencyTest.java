package com.example.exodia.service;

import com.example.exodia.course.domain.Course;
import com.example.exodia.course.dto.CourseCreateDto;
import com.example.exodia.course.service.CourseService;
import com.example.exodia.registration.dto.RegistrationDto;
import com.example.exodia.registration.service.RegistrationService;
import com.example.exodia.user.domain.HireType;
import com.example.exodia.user.dto.UserRegisterDto;
import com.example.exodia.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class CourseRegistrationConcurrencyTest {

    @Autowired
    private UserService userService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long courseId;

    @BeforeEach
    @Transactional
    public void setUp() {
        // 관리자 유저(20240901001)를 SecurityContextHolder에 설정하여 강좌 생성
        setAuthenticationForUser("20240901001");

        // 강좌 생성
        CourseCreateDto courseCreateDto = CourseCreateDto.builder()
                .courseName("Test Course")
                .courseUrl("http://example.com/course")
                .maxParticipants(100) // 최대 참가자 수 설정
                .build();
        Course createdCourse = courseService.createCourse(courseCreateDto);
        courseId = createdCourse.getId();

        // 500명의 유저 생성 및 DB 저장
        createTestUsers();
    }

    private void setAuthenticationForUser(String userNum) {
        // SecurityContextHolder에 인증 정보 설정
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userNum, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // 500명의 유저를 생성하고 DB에 저장하는 메서드
    @Transactional
    public void createTestUsers() {
        for (int i = 4; i <= 303; i++) {
            String userNum = String.format("20240901%03d", i);
            createAndSaveTestUser(userNum, "Test User" + i, "M", "재직", "testtest", "test" + i + "@test.com", "서울시", "01012345678", "123456-1234567", 1L, 2L, 10);
        }
    }

    @Transactional
    public void createAndSaveTestUser(String userNum, String name, String gender, String status, String password, String email, String address, String phone, String socialNum, Long departmentId, Long positionId, int annualLeave) {
        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setUserNum(userNum);
        userRegisterDto.setName(name);
        userRegisterDto.setGender(gender);
        userRegisterDto.setStatus(status);
        userRegisterDto.setPassword(passwordEncoder.encode(password));
        userRegisterDto.setEmail(email);
        userRegisterDto.setAddress(address);
        userRegisterDto.setPhone(phone);
        userRegisterDto.setSocialNum(socialNum);
        userRegisterDto.setDepartmentId(departmentId);
        userRegisterDto.setPositionId(positionId);
        userRegisterDto.setAnnualLeave(annualLeave);

        // hireType 필드 설정 (정규직 혹은 계약직 등의 값으로 설정)
        userRegisterDto.setHireType(HireType.정규직); // 적절한 HireType 설정

        // UserService를 통해 유저를 DB에 저장
        userService.createAndSaveTestUser(userRegisterDto, null); // profileImage는 null로 처리
    }

    @Test
    public void testConcurrentRegistrations() throws InterruptedException {
        int threadCount = 300;
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int userIndex = i;
            executorService.submit(() -> {
                try {
                    String userNum = String.format("20240901%03d", userIndex + 4);
                    setAuthenticationForUser(userNum);

                    try {
                        // 참가자 등록
                        String result = registrationService.registerParticipant(courseId);

                        // Redis나 DB에서 현재 참가자 수 가져오기
                        int currentParticipants = registrationService.getCurrentParticipantCount(courseId);

                        // 등록 성공 또는 초과 메시지 출력
                        if (result.contains("초과")) {
                            System.out.printf("유저 %s 은 등록에 실패하였습니다: %s\n", userNum, result);
                        } else {
                            System.out.printf("유저 %s 은 등록에 성공하였습니다 ( %d / 100 )\n", userNum, currentParticipants);
                        }
                    } catch (Exception e) {
                        System.out.printf("유저 %s 은 등록에 실패하였습니다: %s\n", userNum, e.getMessage());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executorService.shutdown();

        // 최종 확인: 참가자가 100명이 넘지 않는지 확인
        List<RegistrationDto> confirmedParticipants = registrationService.getConfirmedParticipants(courseId);
        assertEquals(100, confirmedParticipants.size(), "강좌의 최대 참가자는 100명이어야 합니다.");
    }
}
