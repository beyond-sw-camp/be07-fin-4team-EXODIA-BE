package com.example.exodia.chat.service;

import com.example.exodia.chat.dto.ChatMessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> chatRedisTemplate;
    private final SimpMessageSendingOperations messageSendingOperations;

    @Autowired
    public RedisSubscriber(ObjectMapper objectMapper, @Qualifier("chat") RedisTemplate<String, Object> chatRedisTemplate, SimpMessageSendingOperations messageSendingOperations) {
        this.objectMapper = objectMapper;
        this.chatRedisTemplate = chatRedisTemplate;
        this.messageSendingOperations = messageSendingOperations;
    }

    // Redis에서 메시지가 발행(publish)되면 대기하고 있던 onMessage가 메시지를 받아 messagingTemplate를 이용하여 websocket 클라이언트들에게 메시지 전달

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // redis에서 발행된 데이터를 받아 역직렬화
            String publishMessage = (String) chatRedisTemplate.getStringSerializer().deserialize(message.getBody());

            // ChatMessageResponse 객채로 맵핑
            ChatMessageResponse roomMessage = objectMapper.readValue(publishMessage, ChatMessageResponse.class);

            // Websocket 구독자에게 채팅 메시지 전송
            messageSendingOperations.convertAndSend("/topic/chat/room/" + roomMessage.getRoomId(), roomMessage);

        }catch (Exception e){
            log.error(e.getMessage());
            // throw new ChatMessageNotFoundException(); // exception을 만들거나 맞는 예외 던져주기
        }
    }
}
