package com.example.exodia.notification.repository;

import com.example.exodia.notification.domain.Notification;
import com.example.exodia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    boolean existsByUserAndMessage(User user, String message);

    // 사용자 읽지 않은 알림 개수 조회
    long countByUserAndIsReadFalse(User user);
    // 사용자 읽지 않은 알림 목록 조회
    List<Notification> findByUserAndIsReadFalse(User user);
}
