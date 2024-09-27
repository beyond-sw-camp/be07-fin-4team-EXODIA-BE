package com.example.exodia.chat.service;

import com.example.exodia.chat.domain.ChatRoom;
import com.example.exodia.chat.dto.ChatMessageResponse;
import com.example.exodia.chat.dto.ChatRoomRequest;
import com.example.exodia.chat.dto.ChatRoomResponse;
import com.example.exodia.chat.repository.ChatMessageRepository;
import com.example.exodia.chat.repository.ChatRoomRepository;
import com.example.exodia.chat.repository.ChatUserRepository;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.dto.UserDto;
import com.example.exodia.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
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

    @Transactional
    public Long createChatRoom(ChatRoomRequest chatRoomRequest){
        // 채팅방 만들기
        // 생성-채팅방저장, 채팅유저저장

        // 유저들 존재여부 확인
        List<User> participants = new ArrayList<>();
        for(String userNum : chatRoomRequest.getUserNums()){
            participants.add(userRepository.findByUserNumAndDelYn(userNum, DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다.")));
        }

        // 채팅방 있는지 확인 // 인원 중복 단체채팅방 이름바꿔 생성 가능
        if(participants.size() )
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        for()



        return 1L;
    }

    public List<ChatRoomResponse> viewChatRoomList(String userNum){
        // 유저가 포함되어 있는 모든 채팅방 리스트 조회

    }

    public List<ChatMessageResponse> viewChatMessageList(Long roomId){
        // 채팅방 입장시 메세지 조회

    }

    public List<UserDto> viewChatUserList(Long roomId){
        // 채팅방-채팅유저 조회

    }

    public void deleteChatRoom(Long roomId){
        // 채팅방 삭제

    }
}
