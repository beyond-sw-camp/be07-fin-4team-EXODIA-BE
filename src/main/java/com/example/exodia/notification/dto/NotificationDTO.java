package com.example.exodia.notification.dto;

import com.example.exodia.notification.domain.NotificationType;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO implements Serializable {
    private static final long serialVersionUID = 2713346395385810094L;
    private Long id; // redis 고유값으로 사용할꺼
    private NotificationType type;
    private String message;
    private boolean isRead;
    private String userName;
    private String userNum;
    private LocalDateTime notificationTime;
    private Long targetId; // 알림 라우팅 경로

    public NotificationDTO(String message, boolean isRead, String userName, String userNum, NotificationType type) {
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.userName = userName;
        this.userNum = userNum;
        this.notificationTime = LocalDateTime.now();
    }

}