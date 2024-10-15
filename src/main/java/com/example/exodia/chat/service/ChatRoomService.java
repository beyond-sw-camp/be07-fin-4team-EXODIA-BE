package com.example.exodia.chat.service;

import com.example.exodia.chat.domain.ChatMessage;
import com.example.exodia.chat.domain.ChatRoom;
import com.example.exodia.chat.domain.ChatUser;
import com.example.exodia.chat.dto.*;
import com.example.exodia.chat.repository.ChatMessageRepository;
import com.example.exodia.chat.repository.ChatRoomRepository;
import com.example.exodia.chat.repository.ChatUserRepository;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ChatRoomService {

    private final ChatRoomManage chatRoomManage; // redis로 채팅룸 입장유저들 관리 -> 읽음처리
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;
    private final UserRepository userRepository;

    @Autowired
    public ChatRoomService(ChatRoomManage chatRoomManage, ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository, ChatUserRepository chatUserRepository, UserRepository userRepository) {
        this.chatRoomManage = chatRoomManage;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatUserRepository = chatUserRepository;
        this.userRepository = userRepository;
    }

    // 인원 중복 채팅방 조회
    public Long findExistChatRoom(Set<String> requestUserNums, List<ChatRoom> existChatRooms){
        for(ChatRoom chatRoom : existChatRooms){
            Set<String> existUserNums = chatRoom.getChatUsers().stream().map(chatUser->chatUser.getUser().getUserNum()).collect(Collectors.toSet());
            if(requestUserNums.equals(existUserNums)){
                log.info("해당 인원들이 포함된 채팅방이 이미 존재합니다.");
                return chatRoom.getId();
            }
        }
        return 0L;
    }

    // 채팅방 만들기
    public ChatRoomExistResponse createChatRoom(ChatRoomRequest chatRoomRequest){
        // 유저들 존재여부 확인 -> 채팅구성원
        List<User> participants = new ArrayList<>();
        // 채팅방 생성한 유저는 맨처음에 들어간다.
        participants.add(userRepository.findByUserNumAndDelYn(chatRoomRequest.getUserNum(), DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다.")));
        // 나머지 채팅방에 초대된 인원
        for(String userNum : chatRoomRequest.getUserNums()){
            participants.add(userRepository.findByUserNumAndDelYn(userNum, DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다.")));
        }

        // 채팅방 있는지 확인 // 인원 중복 단체채팅방 생성 불가능
        Set<String> requestUserNums = new HashSet<>(chatRoomRequest.getUserNums());
        requestUserNums.add(chatRoomRequest.getUserNum()); // 채팅방 구성원의 사번을 모은 set

        List<ChatUser> chatUsers = chatUserRepository.findAllByUser(participants.get(0));
        // 채팅유저가 있다 == 채팅방 생성 유저가 속한 채팅방이 있다
        if(!chatUsers.isEmpty()){
            List<ChatRoom> existChatRooms = chatUsers.stream().map(ChatUser::getChatRoom).toList(); // 채팅방을 조회할 때 채팅방 생성유저가 속한 채팅방만 가져간다. // List<ChatRoom> existChatRooms = chatRoomRepository.findAll();
            Long checkRoomId = findExistChatRoom(requestUserNums, existChatRooms);
            if(checkRoomId != 0L){ // 그중에 중복 채팅방이 있다. 기존 채팅방을 반환
                ChatRoom existedRoom = chatRoomRepository.findById(checkRoomId)
                        .orElseThrow(()->new EntityNotFoundException("없는 채팅방 입니다."));
                return existedRoom.fromEntityExistChatRoom(true);
            }
        }

        // 채팅방을 만드려는 유저가 속한 채팅방이 없거나 // 있는데 중복 채팅방 없으면 신규 생성
        // 새로운 채팅방 생성- 1. 채팅방저장
        ChatRoom savedChatRoom = chatRoomRequest.toEntity();
        chatRoomRepository.save(savedChatRoom);
        // 새로운 채팅방 생성 - 2. 채팅유저저장
        for(User user : participants){
            ChatUser savedChatUser = ChatUser.toEntity(savedChatRoom, user);
            chatUserRepository.save(savedChatUser);
            savedChatRoom.setChatUsers(savedChatUser);
        }
        return savedChatRoom.fromEntityExistChatRoom(false);
    }

    // 채팅방 목록 조회 ⭐⭐⭐ 어떤 기준으로 리스트 업할것인가.. res부터 새로 만들어야할수도..
    public List<ChatRoomResponse> viewChatRoomList(String userNum){
        // 채팅방 목록 조회하는 유저 확인
        User user = userRepository.findByUserNum(userNum).orElseThrow(()->new EntityNotFoundException("없는 사원입니다."));
        // 유저가 포함되어 있는 모든 채팅방 리스트 조회
        List<ChatUser> chatUsers = chatUserRepository.findAllByUser(user);
        List<ChatRoom> chatRooms = chatUsers.stream().map(ChatUser::getChatRoom).toList();

        return chatRooms.stream().map(ChatRoom::fromEntity).collect(Collectors.toList());
    }

    public List<ChatMessageResponse> viewChatMessageList(Long roomId){
        // ⭐⭐⭐ 채팅방 입장시 메세지 조회 // 어떤 순서를 기준으로 리스트업할것인가 // 수정필요 // 쿼리문
        return chatMessageRepository.findAllByChatRoomId(roomId)
                .stream().map(ChatMessage::fromEntityForChatList).collect(Collectors.toList());
    }

//    public List<UserDto> viewChatUserList(Long roomId){
//        // 채팅방-채팅유저 조회 // userService 사용?
//
//    }
//
//    public void inviteChatUser(){
//        // 채팅 유저 초대
//    }
//
//    public void quitChatRoom(Long roomId){
//        // 채팅방 완전히 나가기 // 채팅 유저에서 삭제
//
//
//    }

}
