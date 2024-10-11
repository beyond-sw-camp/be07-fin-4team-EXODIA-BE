package com.example.exodia.chat.service;

import com.example.exodia.chat.domain.ChatMessage;
import com.example.exodia.chat.domain.ChatRoom;
import com.example.exodia.chat.domain.MessageType;
import com.example.exodia.chat.dto.ChatMessageRequest;
import com.example.exodia.chat.dto.ChatMessageResponse;
import com.example.exodia.chat.repository.ChatMessageRepository;
import com.example.exodia.chat.repository.ChatRoomRepository;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.*;

@Service
@Slf4j
public class ChatMessageService {
    private final ChatRoomManage chatRoomManage; // redis로 채팅룸 입장유저들 관리
    @Qualifier("chat")
    private final RedisTemplate<String, Object> chatredisTemplate;
    @Qualifier("chat")
    private final ChannelTopic channelTopic;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    @Autowired
    public ChatMessageService(ChatRoomManage chatRoomManage, @Qualifier("chat") RedisTemplate<String, Object> chatredisTemplate, @Qualifier("chat") ChannelTopic channelTopic, ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository, UserRepository userRepository, FileUploadService fileUploadService) {
        this.chatRoomManage = chatRoomManage;
        this.chatredisTemplate = chatredisTemplate;
        this.channelTopic = channelTopic;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
        this.fileUploadService = fileUploadService;
    }

    // 채팅방에 메세지 발송
    @Transactional
    public void sendMessage(ChatMessageRequest chatMessageRequest){
        // 보낸 사람
        User user = userRepository.findByUserNumAndDelYn(chatMessageRequest.getSenderNum(), DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 사원입니다"));
        // 채팅방
        ChatRoom chatRoom = chatRoomRepository.findById(chatMessageRequest.getRoomId()).orElseThrow(()->new EntityNotFoundException("없는 채팅방입니다."));

        // 채팅 메세지 db 저장
        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessageRequest.toEntity(user, chatRoom));

        // 파일을 포함한 메세지일 경우, 파일메타데이터 db에 저장
        // 전송할 messageRes 조정
        ChatMessageResponse chatMessageResponse = new ChatMessageResponse();
        if(chatMessageRequest.getMessageType() == MessageType.FILE){
            savedChatMessage.setChatFiles(fileUploadService.saveChatFileMetaData(savedChatMessage, chatMessageRequest.getFiles()));
            chatMessageResponse = savedChatMessage.fromEntityWithFile();
        }else{
            chatMessageResponse = savedChatMessage.fromEntity();
        }

        // publish
        chatredisTemplate.convertAndSend(channelTopic.getTopic(), chatMessageResponse);
    }
}
