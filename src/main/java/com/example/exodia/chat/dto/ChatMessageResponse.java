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
public class ChatMessageResponse { // 보여주는 값 // 프론트에 넘겨주는 값

//    private String profileImageUrl; // 수정필요
//    private String department; // 수정 필요
    private String sendUserNum;
    private String sendName;
//    private String position; // 수정필요

    private Long roomId; // pubsub보내온 dto이랑 리스트업하는 dto랑 구분해야하나
//    private String roomName; // pubsub보내온 dto이랑 리스트업하는 dto랑 구분해야하나

    private MessageType messageType;

    private String message; // file(image)일 경우 url이 들어간다.

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm", timezone = "Asia/Seoul")
    private LocalDateTime sendAt;

}
