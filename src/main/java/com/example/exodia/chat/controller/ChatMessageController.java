package com.example.exodia.chat.controller;

import com.example.exodia.chat.dto.ChatMessageRequest;
import com.example.exodia.chat.service.ChatMessageService;
import com.example.exodia.common.auth.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final JwtTokenProvider jwtTokenProvider;
    @Autowired
    public ChatMessageController(ChatMessageService chatMessageService, JwtTokenProvider jwtTokenProvider) {
        this.chatMessageService = chatMessageService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // websocket "/app(pub)/chat/message"로 들어오는 메시징을 처리한다.
    @MessageMapping("/chat/message") // @MessageMapping("/{chatRoomId}") @MessageMapping("/chat/message") // /pub/1 // @DestinationVariable(value = "chatRoomId") Long chatRoomId
    public void sendMessage(ChatMessageRequest chatMessageRequest){
        chatMessageService.sendMessage(chatMessageRequest); // 토큰도 확인. 수정필요 기둘
    }
}
