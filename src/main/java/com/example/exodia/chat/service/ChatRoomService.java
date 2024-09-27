package com.example.exodia.chat.service;

import com.example.exodia.chat.dto.ChatMessageResponse;
import com.example.exodia.chat.dto.ChatRoomRequest;
import com.example.exodia.chat.dto.ChatRoomResponse;
import com.example.exodia.chat.repository.ChatMessageRepository;
import com.example.exodia.chat.repository.ChatRoomRepository;
import com.example.exodia.chat.repository.ChatUserRepository;
import com.example.exodia.user.dto.UserDto;
import com.example.exodia.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ChatRoomService {

    private final ChatRoomManage chatRoomManage; // redis로 채팅룸 입장유저들 관리
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;
    private final UserRepository userRepository;

    public ChatRoomService(ChatRoomManage chatRoomManage, ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository, ChatUserRepository chatUserRepository, UserRepository userRepository) {
        this.chatRoomManage = chatRoomManage;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatUserRepository = chatUserRepository;
        this.userRepository = userRepository;
    }

    public Long createChatRoom(ChatRoomRequest chatRoomRequest){
        // 유저들 존재여부 확인
        // 채팅방 있는지 확인
        // 있으면 입장 없으면 생성-채팅방저장, 채팅유저저장
        return 1L;
    }

    public List<ChatRoomResponse> viewChatRoomList(String userNum){
        //

    }

    public List<ChatMessageResponse> viewChatMessageList(Long roomId){

    }

    public List<UserDto> viewChatUserList(Long roomId){

    }

    public void deleteChatRoom(Long roomId){

    }
}
