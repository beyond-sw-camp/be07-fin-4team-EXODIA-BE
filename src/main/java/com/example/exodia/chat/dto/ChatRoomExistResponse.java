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
public class ChatRoomExistResponse { // chatRoom 만들 때 쓴다.
    private boolean existCheck; // true 면 중복 // false 면 새로운 채팅방
    private Long roomId;
    private String roomName;
    private List<String> userNums; // 채팅유저정보
}
