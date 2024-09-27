package com.example.exodia.reservationMeetTest;

import com.example.exodia.reservationMeeting.domain.ReservationMeet;
import com.example.exodia.reservationMeeting.dto.ReservationMeetCreateDto;
import com.example.exodia.reservationMeeting.dto.ReservationMeetListDto;
import com.example.exodia.reservationMeeting.repository.ReservationMeetRepository;
import com.example.exodia.reservationMeeting.service.ReservationMeetService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class ReservationMeetPessimisticLockTest {

    private static final Logger logger = LoggerFactory.getLogger(ReservationMeetPessimisticLockTest.class);

    @Autowired
    private ReservationMeetService reservationMeetService;

    @Autowired
    private ReservationMeetRepository reservationMeetRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void testPessimisticLock() throws InterruptedException {
        logger.info("===== 테스트 시작 =====");

        // Given: 이미 저장된 회의실 예약 생성
        ReservationMeetCreateDto dto = new ReservationMeetCreateDto();
        dto.setMeetingRoomId(1L);
        dto.setUserId(1L);
        dto.setStartTime(LocalDateTime.of(2024, 9, 27, 10, 0));
        dto.setEndTime(LocalDateTime.of(2024, 9, 27, 11, 0));
        ReservationMeetListDto createdReservation = reservationMeetService.createReservation(dto);

        // 스레드 풀과 CountDownLatch 설정
        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // 각 스레드에서 동일한 시간대에 예약 시도
        for (int i = 0; i < numberOfThreads; i++) {
            int threadNumber = i + 1;
            executor.execute(() -> {
                logger.info("스레드 {} 예약 시도 시작", threadNumber);
                try {
                    createReservationWithPessimisticLock(createdReservation.getId());
                    logger.info("스레드 {} 예약 성공", threadNumber);
                } catch (Exception e) {
                    logger.error("스레드 {} 예약 실패: {}", threadNumber, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드가 작업을 완료할 때까지 대기
        latch.await();
        logger.info("모든 스레드 작업 완료");

        // Then: 최종 예약 개수가 1개인지 확인 (락으로 인해 중복 예약이 없어야 함)
        long reservationCount = reservationMeetRepository.count();
        logger.info("최종 예약 개수 확인: {}", reservationCount);
        Assertions.assertEquals(1, reservationCount);

        logger.info("===== 테스트 종료 =====");
    }

    @Transactional
    public void createReservationWithPessimisticLock(Long reservationId) {
        EntityManager em = entityManagerFactory.createEntityManager();

        em.getTransaction().begin();
        logger.info("트랜잭션 시작");

        try {
            logger.info("비관적 락으로 예약 조회 시도, 예약 ID: {}", reservationId);
            ReservationMeet reservation = em.find(ReservationMeet.class, reservationId, LockModeType.PESSIMISTIC_WRITE);
            logger.info("예약 조회 성공: {}", reservation);

            // 비관적 락 테스트를 위한 인위적 지연
            Thread.sleep(2000);
            logger.info("2초 대기 후 트랜잭션 커밋 시도");
            em.getTransaction().commit();
            logger.info("트랜잭션 커밋 성공");
        } catch (Exception e) {
            logger.error("트랜잭션 중 오류 발생: {}", e.getMessage());
            em.getTransaction().rollback();
            logger.info("트랜잭션 롤백 완료");
            throw new RuntimeException(e);
        } finally {
            em.close();
            logger.info("EntityManager 닫음");
        }
    }
}
