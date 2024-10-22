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

    private String senderName;

    private String roomName;

    private String message;

}
