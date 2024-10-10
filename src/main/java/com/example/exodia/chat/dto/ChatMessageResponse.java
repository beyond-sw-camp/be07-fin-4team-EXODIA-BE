package com.example.exodia.chat.dto;

import com.example.exodia.chat.domain.MessageType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse { // 보여주는 값 // 프론트에 넘겨주는 값

    private String senderNum;
    private String senderName;
    private String senderDepName;
    private String senderPosName;

    private Long roomId;

    private MessageType messageType;

    private String message;

    private List<ChatFileMetaDataResponse> files;

    private String createAt;
}
