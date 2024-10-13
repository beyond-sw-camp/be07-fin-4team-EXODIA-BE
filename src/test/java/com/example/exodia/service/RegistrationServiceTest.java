//package com.example.exodia.service;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//import com.example.exodia.common.service.KafkaProducer;
//import com.example.exodia.course.domain.Course;
//import com.example.exodia.course.repository.CourseRepository;
//import com.example.exodia.registration.service.RegistrationService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
//class RegistrationServiceTest {
//
//    @Mock
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Mock
//    private ValueOperations<String, Object> valueOperations;
//
//    @Mock
//    private KafkaProducer kafkaProducer;
//
//    @Mock
//    private CourseRepository courseRepository;
//
//    @InjectMocks
//    private RegistrationService registrationService;
//
//    private final int maxParticipants = 100;
//    private final AtomicInteger currentParticipants = new AtomicInteger(0); // 현재 참가자 수
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//    }
//
//    @Test
//    void testConcurrentUserRegistration() throws InterruptedException, ExecutionException {
//
//        Course mockCourse = new Course();
//        mockCourse.setMaxParticipants(maxParticipants);
//        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(mockCourse));
//
//        when(valueOperations.increment(anyString())).thenAnswer(invocation -> (long) currentParticipants.incrementAndGet());
//
//        // 10,000명의 사용자 처리하기 위해 ExecutorService 사용
//        ExecutorService executorService = Executors.newFixedThreadPool(100); // 100개의 스레드 풀
//        List<Callable<String>> tasks = new ArrayList<>();
//
//        // 10,000명의 사용자 등록 시도
//        for (int i = 1; i <= 10000; i++) {
//            final long userId = i;
//            tasks.add(() -> {
//                synchronized (currentParticipants) {
//                    String result = registrationService.registerParticipant(1L, userId);
//                    // 성공한 사람만 출력
//                    if ("등록 완료".equals(result)) {
//                        int current = currentParticipants.get();
//                        System.out.println("User " + userId + "가 등록에 성공했습니다. (" + current + "/" + maxParticipants + ")");
//                    } else {
//                        System.out.println("User " + userId + "가 등록에 실패했습니다.");
//                    }
//                    return result;
//                }
//            });
//        }
//
//        // 10,000명의 사용자 등록 요청 실행
//        List<Future<String>> futures = executorService.invokeAll(tasks);
//        executorService.shutdown();
//
//        // 성공한 사용자 수 확인
//        long successCount = futures.stream().filter(future -> {
//            try {
//                return "등록 완료".equals(future.get());
//            } catch (Exception e) {
//                return false;
//            }
//        }).count();
//
//        // 성공한 사용자 수가 정확히 100명인지 확인
//        assertEquals(maxParticipants, successCount);
//    }
//}
