//package com.example.exodia.service;
//
//import com.example.exodia.course.domain.Course;
//import com.example.exodia.course.dto.CourseCreateDto;
//import com.example.exodia.course.service.CourseService;
//import com.example.exodia.department.domain.Department;
//import com.example.exodia.department.repository.DepartmentRepository;
//import com.example.exodia.position.domain.Position;
//import com.example.exodia.position.repository.PositionRepository;
//import com.example.exodia.registration.service.RegistrationService;
//import com.example.exodia.user.domain.Gender;
//import com.example.exodia.user.domain.HireType;
//import com.example.exodia.user.domain.NowStatus;
//import com.example.exodia.user.domain.Status;
//import com.example.exodia.user.dto.UserRegisterDto;
//import com.example.exodia.user.service.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.stream.IntStream;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//
//@SpringBootTest
//public class RegistrationServiceConcurrencyTest {
//
//    @Autowired
//    private RegistrationService registrationService;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private CourseService courseService;
//
//    @Autowired
//    private DepartmentRepository departmentRepository;
//
//    @Autowired
//    private PositionRepository positionRepository;
//
//    @BeforeEach
//    public void setUp() {
//        Authentication authentication = Mockito.mock(Authentication.class);
//        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
//        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
//        Mockito.when(authentication.getName()).thenReturn("20240901001");
//        SecurityContextHolder.setContext(securityContext);
//    }
//
//    @Test
//    public void testConcurrentRegistrations() throws Exception {
//        createTestUsers(500);
//
//        // Step 2: 관리자 권한으로 강좌 생성
//        CourseCreateDto courseCreateDto = new CourseCreateDto();
//        courseCreateDto.setCourseName("Test Course");
//        courseCreateDto.setCourseUrl("http://example.com");
//        courseCreateDto.setMaxParticipants(100);
//
//        // 관리자 권한을 가진 사용자로 강좌 생성
//        Course createdCourse = courseService.createCourse(courseCreateDto);
//
//        ExecutorService executorService = Executors.newFixedThreadPool(100);
//        List<Future<?>> futures = new ArrayList<>();
//
//        IntStream.rangeClosed(1, 500).forEach(i -> {
//            Future<?> future = executorService.submit(() -> {
//                String userNum = "2024" + String.format("%06d", i);
//
//                Authentication userAuthentication = Mockito.mock(Authentication.class);
//                Mockito.when(userAuthentication.getName()).thenReturn(userNum);
//                SecurityContextHolder.getContext().setAuthentication(userAuthentication);
//
//                registrationService.registerParticipant(createdCourse.getId());
//            });
//            futures.add(future);
//        });
//
//        for (Future<?> future : futures) {
//            future.get();
//        }
//        executorService.shutdown();
//
//        assertEquals(100, registrationService.getConfirmedParticipants(createdCourse.getId()).size(), "Ensure only 100 users registered successfully");
//    }
//
//    public void createTestUsers(int numberOfUsers) {
//        Department testDepartment = departmentRepository.findByName("경영1팀")
//                .orElseThrow(() -> new RuntimeException("경영1팀 존재하지 않습니다."));
//        Position testPosition = positionRepository.findByName("사원")
//                .orElseThrow(() -> new RuntimeException("사원 직급이 존재하지 않습니다."));
//
//        for (int i = 1; i <= numberOfUsers; i++) {
//            UserRegisterDto registerDto = new UserRegisterDto();
//            registerDto.setUserNum("2024" + String.format("%06d", i));
//            registerDto.setName("테스트유저" + i);
//            registerDto.setDepartmentId(testDepartment.getId());
//            registerDto.setPositionId(testPosition.getId());
//
//            registerDto.setGender(Gender.M);
//            registerDto.setHireType(HireType.정규직);
//            registerDto.setStatus(Status.재직);
//            registerDto.setPassword("password" + i);
//            registerDto.setEmail("testuser" + i + "@example.com");
//            registerDto.setPhone("01012341234");
//            registerDto.setSocialNum("123456-1234567");
//
//            userService.registerUser(registerDto, null, testDepartment.getId().toString());
//        }
//    }
//}