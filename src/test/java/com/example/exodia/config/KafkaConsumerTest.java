package com.example.exodia.config;

import com.example.exodia.common.service.KafkaConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

class KafkaConsumerTest {

    @InjectMocks
    private KafkaConsumer kafkaConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListenCourseRegistration() {
        // 직접 메서드 호출
        kafkaConsumer.listenCourseRegistration("course-registration", "User 123 has registered for course 1");

        // 실제 처리 로직에 대한 검증 (예: 로그 메시지 또는 DB 저장 등의 로직)
    }
}
