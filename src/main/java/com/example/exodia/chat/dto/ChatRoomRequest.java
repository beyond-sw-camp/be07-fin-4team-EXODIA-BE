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
public class ChatRoomRequest { // 받아오는 값
    private String roomName;
    private List<String> userNums; // 채팅유저정보 (시작한 사람 포함?)
}
