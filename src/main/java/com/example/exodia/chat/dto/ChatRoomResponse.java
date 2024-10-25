package com.example.exodia.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomResponse { // 보여지는 것
    private Long roomId;
    private String roomName;
    private List<String> userNums; // 채팅유저정보
    private int unreadChatNum;
    private String recentChat;
    private String recentChatTime;
}
