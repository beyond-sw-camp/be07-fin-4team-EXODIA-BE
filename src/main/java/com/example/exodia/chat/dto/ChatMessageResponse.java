package com.example.exodia.chat.dto;

import com.example.exodia.chat.domain.MessageType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse { // 보여주는 값

    private String sendUserNum;
    private String name;

    private Long roomId;
    private String roomName;

    private MessageType messageType;

    private String message; // file(image)일 경우 url이 들어간다.

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm", timezone = "Asia/Seoul")
    private LocalDateTime sendAt;

}
