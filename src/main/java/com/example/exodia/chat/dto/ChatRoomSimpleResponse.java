package com.example.exodia.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomSimpleResponse {
    private Long roomId;
    private String roomName;
    private Integer userNumbers; // 채팅유저수
}
