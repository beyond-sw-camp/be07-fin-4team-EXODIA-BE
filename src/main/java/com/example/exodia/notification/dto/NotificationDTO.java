package com.example.exodia.notification.dto;

import com.example.exodia.notification.domain.Notification;
import com.example.exodia.notification.domain.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {

    private Long id;
    private NotificationType type;
    private String message;
    private boolean isRead;
    private String userName;
    private LocalDateTime notificationTime;

    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.type = notification.getType();
        this.message = notification.getMessage();
        this.isRead = notification.getIsRead();
        this.userName = notification.getUser().getName();
    }

    public static NotificationDTO fromEntity(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getUser().getName(),
                notification.getCreatedAt()
        );
    }
}
