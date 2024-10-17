package com.example.exodia.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatRoomManage { // redis로 채팅룸 입장유저들 관리
    @Qualifier("chatRoom")
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public ChatRoomManage(@Qualifier("chatRoom") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getChatroomIdByUser(String userNum){
        try {
            return (String) redisTemplate.opsForValue().get("user_" + userNum);
        }catch (Exception e){
            return null;
        }
    }

    // 유저 채팅방 입장
    public Long updateChatRoomId(String userNum, Long chatRoomId){
        redisTemplate.opsForValue().set("user_"+userNum, chatRoomId);
        return chatRoomId;
    }

    // 유저 채팅방 퇴장
    public void exitChatRoom(String userNum){
        redisTemplate.delete("user_"+userNum);
    }

}
