package com.example.exodia.service;

import com.example.exodia.course.domain.Course;
import com.example.exodia.course.dto.CourseCreateDto;
import com.example.exodia.course.service.CourseService;
import com.example.exodia.department.domain.Department;
import com.example.exodia.department.repository.DepartmentRepository;
import com.example.exodia.position.domain.Position;
import com.example.exodia.position.repository.PositionRepository;
import com.example.exodia.registration.service.RegistrationService;
import com.example.exodia.user.domain.Gender;
import com.example.exodia.user.domain.Status;
import com.example.exodia.user.dto.UserRegisterDto;
import com.example.exodia.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RegistrationServiceConcurrencyTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @BeforeEach
    public void setUp() {
        // Mocking SecurityContextHolder to simulate authenticated users in the test
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testConcurrentRegistrations() throws Exception {
        // Step 1: Create 10000 users in the system
        createTestUsers(10000);

        // Step 2: Create a course with a maxParticipants limit of 100
        CourseCreateDto courseCreateDto = new CourseCreateDto();
        courseCreateDto.setCourseName("Test Course");
        courseCreateDto.setCourseUrl("http://example.com");
        courseCreateDto.setMaxParticipants(100);
        Course createdCourse = courseService.createCourse(courseCreateDto);

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        List<Future<?>> futures = new ArrayList<>();

        // Step 3: 10000 users attempt to register for the course
        IntStream.rangeClosed(1, 10000).forEach(i -> {
            Future<?> future = executorService.submit(() -> {
                // Simulate different users by setting userNum manually
                String userNum = "2024" + String.format("%06d", i);

                // Mock the authenticated user in the SecurityContext
                Authentication authentication = Mockito.mock(Authentication.class);
                Mockito.when(authentication.getName()).thenReturn(userNum);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Step 4: Each user tries to register for the course
                registrationService.registerParticipant(createdCourse.getId());
            });
            futures.add(future);
        });

        // Step 5: Wait for all tasks to complete
        for (Future<?> future : futures) {
            future.get();
        }
        executorService.shutdown();

        // Step 6: Validate that only 100 users were successfully registered
        assertEquals(100, registrationService.getConfirmedParticipants(createdCourse.getId()).size(), "Ensure only 100 users registered successfully");
    }

    // Method to create test users for the system
    public void createTestUsers(int numberOfUsers) {
        Department testDepartment = departmentRepository.findByName("경영1팀")
                .orElseThrow(() -> new RuntimeException("경영1팀 존재하지 않습니다."));
        Position testPosition = positionRepository.findByName("사원")
                .orElseThrow(() -> new RuntimeException("사원 직급이 존재하지 않습니다."));

        for (int i = 1; i <= numberOfUsers; i++) {
            UserRegisterDto registerDto = new UserRegisterDto();
            registerDto.setUserNum("2024" + String.format("%06d", i));
            registerDto.setName("테스트유저" + i);
            registerDto.setDepartmentId(testDepartment.getId());
            registerDto.setPositionId(testPosition.getId());
            registerDto.setPassword("password" + i);
            registerDto.setEmail("testuser" + i + "@example.com");
            registerDto.setPhone("010-1234-" + String.format("%04d", i));
            registerDto.setSocialNum("123456-1234567");

            // Register the user
            userService.registerUser(registerDto, null, testDepartment.getId().toString());
        }
    }
}
