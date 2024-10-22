package com.example.exodia.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatUserResponse { // 채팅방 구성원 조회
    private String chatUserNum;
    private String chatUserName;
    private String chatUserDepName;
    private String chatUserPosName;
}
