package com.example.exodia.chat.dto;

import com.example.exodia.chat.domain.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatAlarmResponse {
    private String senderNum;
    private String senderName;
    private String senderDepName;
    private String senderPosName;

    private Long roomId;
    private String roomName;

    private MessageType messageType;

    private String message;

    private String createAt;
}
