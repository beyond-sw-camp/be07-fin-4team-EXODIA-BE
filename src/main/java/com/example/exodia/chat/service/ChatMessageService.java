package com.example.exodia.chat.service;

import com.example.exodia.chat.domain.ChatMessage;
import com.example.exodia.chat.domain.ChatRoom;
import com.example.exodia.chat.domain.MessageType;
import com.example.exodia.chat.dto.ChatMessageRequest;
import com.example.exodia.chat.repository.ChatFileRepository;
import com.example.exodia.chat.repository.ChatMessageRepository;
import com.example.exodia.chat.repository.ChatRoomRepository;
import com.example.exodia.chat.repository.ChatUserRepository;
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

import javax.persistence.EntityNotFoundException;

@Service
@Slf4j
public class ChatMessageService {
    private final ChatRoomManage chatRoomManage; // redis로 채팅룸 입장유저들 관리
    @Qualifier("chatPubSub")
    private final RedisTemplate<String, Object> chatredisTemplate;
    @Qualifier("chatPubSub")
    private final ChannelTopic channelTopic;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatFileRepository chatFileRepository;
    private final ChatUserRepository chatUserRepository;
    private final UserRepository userRepository;

    @Autowired
    public ChatMessageService(ChatRoomManage chatRoomManage, @Qualifier("chatPubSub") RedisTemplate<String, Object> chatredisTemplate, @Qualifier("chatPubSub") ChannelTopic channelTopic, ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository, ChatFileRepository chatFileRepository, ChatUserRepository chatUserRepository, UserRepository userRepository) {
        this.chatRoomManage = chatRoomManage;
        this.chatredisTemplate = chatredisTemplate;
        this.channelTopic = channelTopic;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatFileRepository = chatFileRepository;
        this.chatUserRepository = chatUserRepository;
        this.userRepository = userRepository;
    }

    public void enter(String userNum, Long roomId){

    }

    // 채팅방에 메세지 발송
    @Transactional
    public void sendMessage(ChatMessageRequest chatMessageRequest, String userNum){
        // 보낸 사람
        User user = userRepository.findByUserNumAndDelYn(userNum, DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 사원입니다"));
        // 채팅방
        ChatRoom chatRoom = chatRoomRepository.findByIdAndDelYn(chatMessageRequest.getRoomId(), DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 채팅방입니다."));

        if(chatMessageRequest.getMessageType() == MessageType.TALK){
            ChatMessage savedChatMessage = ChatMessage.toEntity(user, chatRoom, chatMessageRequest);
            chatMessageRepository.save(savedChatMessage);
            String
        }else if(chatMessageRequest.getMessageType() == MessageType.FILE || chatMessageRequest.getMessageType() == MessageType.IMAGE){
            // 메세지 타입이 있는데 ChatFile 테이블이 필요한가..? 메세지에 url 담겨서 올건데..
        }
    }
}
