package com.example.exodia.notification.dto;

import com.example.exodia.notification.domain.NotificationType;
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
    private Long id; // redis 고유값으로 사용할꺼
    private NotificationType type;
    private String message;
    private boolean isRead;
    private String userName;
    private String userNum;
    private LocalDateTime notificationTime;


    public NotificationDTO(String message, boolean isRead, String userName, String userNum, NotificationType type) {
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.userName = userName;
        this.userNum = userNum;
        this.notificationTime = LocalDateTime.now();
    }
}