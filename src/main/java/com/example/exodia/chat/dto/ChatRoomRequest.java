package com.example.exodia.chat.dto;

import com.example.exodia.chat.domain.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomRequest { // 받아오는 값
    private String roomName;
    private String userNum; // 방만드는 사람
    private List<String> userNums; // 초대하는 채팅유저정보

    public ChatRoom toEntity(){
        return ChatRoom.builder()
                .roomName(this.getRoomName())
                .chatUsers(new ArrayList<>())
                .recentChat("")
                .recentChatTime(LocalDateTime.now())
                .build();
    }
}
