package com.example.exodia.chat.dto;

import com.example.exodia.chat.domain.ChatFile;
import com.example.exodia.chat.domain.ChatMessage;
import com.example.exodia.chat.domain.ChatRoom;
import com.example.exodia.chat.domain.MessageType;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageRequest { // 받아오는 값 - 만드는 값

    private String senderNum; // 보내는 사람

    private Long roomId;

    private MessageType messageType;

    private String message;

    private List<ChatFileSaveListDto> files;

    public ChatMessage toEntity(User user, ChatRoom chatRoom){
        return ChatMessage.builder()
                .chatUser(user)
                .chatRoom(chatRoom)
                .messageType(this.getMessageType())
                .message(this.getMessage())
                .chatFiles(new ArrayList<>())
                .build();
    }

}
