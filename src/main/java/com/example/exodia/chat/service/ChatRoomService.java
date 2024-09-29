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

    private final ChatRoomManage chatRoomManage; // redis로 채팅룸 입장유저들 관리 -> 읽음처리...
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

    // 인원 중복 채팅방 조회 // 쿼리문 추가 수정 필요
    public Long findExistChatRoom(Set<String> requestUserNums, List<ChatRoom> exisiChatRooms){
        for(ChatRoom chatRoom : exisiChatRooms){
            Set<String> existUserNums = chatRoom.getChatUsers().stream().map(chatUser->chatUser.getUser().getUserNum()).collect(Collectors.toSet());
            if(requestUserNums.equals(existUserNums)){
                log.info("해당 인원들이 포함된 채팅방이 이미 존재합니다.");
                return chatRoom.getId();
            }
        }
        return 0L;
    }

    public ChatRoomExistResponse createChatRoom(ChatRoomRequest chatRoomRequest){
        // 채팅방 만들기

        // 유저들 존재여부 확인
        List<User> participants = new ArrayList<>();
        for(String userNum : chatRoomRequest.getUserNums()){
            participants.add(userRepository.findByUserNumAndDelYn(userNum, DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 회원입니다.")));
        }

        // 채팅방 있는지 확인 // 인원 중복 단체채팅방 생성 불가능
        Set<String> requestUserNums = new HashSet<>(chatRoomRequest.getUserNums());
        List<ChatRoom> existChatRooms = chatRoomRepository.findAll(); // 성능을 위해 채팅방을 조회하는 쿼리에서 먼저 해당 유저가 포함된 채팅방만 조회하도록 쿼리를 최적화
        Long checkRoomId = findExistChatRoom(requestUserNums, existChatRooms);
        if(checkRoomId != 0L){ // 인원 중복 단체 채팅방이 있다. 기존 채팅방을 반환
            return chatRoomRepository.findByIdAndDelYn(checkRoomId, DelYN.N)
                    .orElseThrow(()->new EntityNotFoundException("없는 채팅방 입니다."))
                    .fromEntityExistChatRoom(true);
        }

        // 중복 채팅방 없음 // 새로운 채팅방 생성-채팅방저장, 채팅유저저장
        ChatRoom savedChatRoom = ChatRoom.toEntity(chatRoomRequest);
        chatRoomRepository.save(savedChatRoom);

        for(User user : participants){
            ChatUser savedChatUser = ChatUser.toEntity(savedChatRoom, user);
            chatUserRepository.save(savedChatUser);
        }

        return savedChatRoom.fromEntityExistChatRoom(false);
    }

    public List<ChatRoomSimpleResponse> viewChatRoomList(String userNum){
        // 유저가 포함되어 있는 모든 채팅방 리스트 조회
        List<ChatUser> chatUsers = chatUserRepository.findAllByUserNumAndDelYn(userNum,DelYN.N);
        List<Long> chatRoomIds = chatUsers.stream().map(p->p.getChatRoom().getId()).distinct().collect(Collectors.toList());

        // 어떤 순서를 기준으로 리스트업할것인가 // 수정필요
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByIdsAndDelYn(chatRoomIds, DelYN.N);

        // 어떤 정보를 넘겨줄 것인가. // 수정필요
        return chatRooms.stream().map(ChatRoom::fromEntitySimpleChatRoom).collect(Collectors.toList());
    }

    public List<ChatMessageResponse> viewChatMessageList(Long roomId){
        // 채팅방 입장시 메세지 조회 // 어떤 순서를 기준으로 리스트업할것인가 // 수정필요 // 쿼리문
        return chatMessageRepository.findAllByRoomIdAndDelYn(roomId, DelYN.N)
                .stream().map(ChatMessage::fromEntity).collect(Collectors.toList());
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
//        // 채팅방 나가기 // 채팅 유저에서 삭제
//
//
//    }

}
