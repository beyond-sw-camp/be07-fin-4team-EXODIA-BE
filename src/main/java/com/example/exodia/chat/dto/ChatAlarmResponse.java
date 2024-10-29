package com.example.exodia.chat.dto;

import com.example.exodia.notification.domain.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatAlarmResponse {

    private String type;

    private String senderName;

    private String roomName;

    private String message;

    private int alarmNum;

}