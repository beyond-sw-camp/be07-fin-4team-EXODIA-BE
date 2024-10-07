package com.example.exodia.notification.dto;

import com.example.exodia.notification.domain.Notification;
import com.example.exodia.notification.domain.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {

    private Long id;
    private NotificationType type;
    private String message;
    private boolean isRead;

    // 필요하면 user의 특정 정보만 포함
    private String userName;

    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.type = notification.getType();
        this.message = notification.getMessage();
        this.isRead = notification.getIsRead();
        this.userName = notification.getUser().getName();
    }

    // Getter, Setter
}
