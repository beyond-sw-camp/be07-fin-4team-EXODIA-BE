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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ChatRoomService {

    private final ChatRoomManage chatRoomManage; // redis로 채팅룸 입장유저들 관리
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;
    private final UserRepository userRepository;

    @Qualifier("chat") // 메세지 pubsub // 각 chatRoom + user의 unread 메세지 개수 관리
    private final RedisTemplate<String, Object> redisTemplate;


    @Autowired
    public ChatRoomService(ChatRoomManage chatRoomManage, ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository, ChatUserRepository chatUserRepository, UserRepository userRepository, RedisTemplate<String, Object> redisTemplate) {
        this.chatRoomManage = chatRoomManage;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatUserRepository = chatUserRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
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

    // 채팅방 생성
    public ChatRoomExistResponse createChatRoom(ChatRoomRequest chatRoomRequest){
        // 유저들 존재여부 확인 -> 채팅방 구성원
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

        List<ChatUser> chatUsers = chatUserRepository.findAllByUser(participants.get(0)); // 채팅방 생성 유저로 chatUser(user-chatRoom) 찾기

        if(!chatUsers.isEmpty()){ // chatUser 있다 == 채팅방 생성 유저가 속한 채팅방이 있다
            List<ChatRoom> existChatRooms = chatUsers.stream().map(ChatUser::getChatRoom).toList(); // 채팅방을 조회할 때 채팅방 생성유저가 속한 채팅방만 가져간다. // List<ChatRoom> existChatRooms = chatRoomRepository.findAll();
            Long checkRoomId = findExistChatRoom(requestUserNums, existChatRooms); // 중복 체크
            if(checkRoomId != 0L){ // 그중에 중복 채팅방이 있다. 기존 채팅방을 반환
                ChatRoom existedRoom = chatRoomRepository.findById(checkRoomId)
                        .orElseThrow(()->new EntityNotFoundException("없는 채팅방 입니다."));
                return existedRoom.fromEntityExistChatRoom(true);
            }
        }

        // 채팅방을 만드려는 유저가 속한 채팅방이 없거나 // 있는데 중복 채팅방 없으면 신규 생성
        // 새로운 채팅방 생성- 1. 채팅방저장
        ChatRoom savedChatRoom = chatRoomRequest.toEntity();
        savedChatRoom.setRecentChatTime(LocalDateTime.now());
        chatRoomRepository.save(savedChatRoom);
        // 새로운 채팅방 생성 - 2. 채팅유저저장
        for(User user : participants){
            ChatUser savedChatUser = ChatUser.toEntity(savedChatRoom, user);
            chatUserRepository.save(savedChatUser);
            savedChatRoom.setChatUsers(savedChatUser);
        }
        return savedChatRoom.fromEntityExistChatRoom(false);
    }

    // 채팅방 목록 조회
    // response에 unread 메세지 개수 추가 -> chatRoom 입장시 localStorage에서 채팅 알림 개수 해당 chatRoom의 unread 메세지 개수 빼기.
    // chatRoom에 최근 온 메세지, 시간 추가 -> 메세지 보낼 때마다 unread 메세지 조정, 최근 온 메세지 조정.
    public List<ChatRoomResponse> viewChatRoomList(String userNum){
        // 채팅방 목록 조회하는 유저 확인
        User user = userRepository.findByUserNum(userNum).orElseThrow(()->new EntityNotFoundException("없는 사원입니다."));
        // 유저가 포함되어 있는 모든 채팅방 리스트 조회
        List<ChatUser> chatUsers = chatUserRepository.findAllByUser(user);
        List<ChatRoom> chatRooms = chatUsers.stream().map(ChatUser::getChatRoom).toList();
        // 최신 메세지 순서대로 (내림차순: 큰값->작은값) 정렬
        chatRooms = chatRooms.stream().sorted(Comparator.comparing(ChatRoom::getRecentChatTime).reversed()).toList();

        List<ChatRoomResponse> chatRoomResponses = new ArrayList<>();

        for(ChatRoom chatRoom : chatRooms){
            String key = "chatRoom_" + chatRoom.getId() + "_" + userNum;

            String unread = (String)redisTemplate.opsForValue().get(key);
            int unreadChat = 0;
//            assert unread != null;
            if(unread != null){
                unreadChat = Integer.parseInt(unread);
            }
            chatRoomResponses.add(chatRoom.fromEntity(unreadChat));
        }

        return chatRoomResponses;
    }

      // ⭐⭐⭐ 채팅방 list에서 검색. 채팅방명, 채팅user이름
//    public List<ChatRoomResponse> searchChatRoom(String searchValue){
//
//    }

    // 채팅방 메세지 조회 == 채팅방 입장
    public List<ChatMessageResponse> viewChatMessageList(Long roomId){
        // chatMessageList를 불러온다 == chatRoom에 입장한다. chatRoomManage(redis로 관리)에 user의 현 채팅방id 기록
        // chatMessageList를 불러온다 == 입장 유저가 확인하지 않은 채팅을 읽는다. 채팅방의 unread 메세지(redis로 관리)의 "chatRoom_" + roomId + "_" + userNum 삭제.

        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        chatRoomManage.updateChatRoomId(userNum, roomId);
        // alarm개수 해당 채팅방의 unread 개수만큼 감소, 채팅방의 unread 메세지 개수 삭제
        String key = "chatRoom_" + roomId + "_" + userNum;
        String unread = (String) redisTemplate.opsForValue().get(key);
        String alarm = chatRoomManage.getChatAlarm(userNum);
        if(unread!=null && alarm!=null){
            chatRoomManage.updateChatAlarm(userNum, Integer.parseInt(alarm) - Integer.parseInt(unread));
        }
        redisTemplate.delete(key);

        return chatMessageRepository.findAllByChatRoomId(roomId)
                .stream().map(ChatMessage::fromEntityForChatList).collect(Collectors.toList());
    }

    // 채팅방 퇴장
    public String exitChatRoom(String userNum){
        // chatRoom을 나올 때 chatRoomManage에 userNum-채팅방id 삭제
        User user = userRepository.findByUserNum(userNum).orElseThrow(()->new EntityNotFoundException("없는 사원입니다."));
        chatRoomManage.exitChatRoom(userNum);
        return userNum;
    }

    // 채팅방 삭제 == 나가려는 chatRoomId(chatRoom)의 userNum(User)을 가진 chatUser 삭제.
    public String  hardExitChatRoom(String userNum, Long roomId){
        User user = userRepository.findByUserNum(userNum).orElseThrow(()->new EntityNotFoundException("없는 사원입니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("채팅방이 없습니다."));
        List<ChatUser> chatUsers = chatUserRepository.findAllByChatRoom(chatRoom);

        if(chatUsers.isEmpty()){ // 채팅방에 참여한 chatUser가 없다. -> 채팅방 삭제
            chatRoom.softDelete();
        }else{
            if(chatUserRepository.findByUserAndChatRoom(user, chatRoom).isPresent()){
                chatUserRepository.findByUserAndChatRoom(user, chatRoom)
                        .orElseThrow(()->new EntityNotFoundException("없는 채팅 유저 입니다."))
                        .softDelete();

                // 채팅방 삭제 in 채팅방 메뉴 // 채팅방을 나간다. -> user의 chatRoom 현황 업데이트
                chatRoomManage.exitChatRoom(userNum);
                // 채팅방의 unread 메세지 삭제
                String key = "chatRoom_" + roomId + "_" + userNum;
                redisTemplate.delete(key);
            }
        }
        // ⭐⭐⭐ 채팅방에 메세지 남겨야하나? oo님이 퇴장하셨습니다. 메세지 발송?

        return userNum;
    }

    // 채팅방 구성원 조회
    public List<ChatUserResponse> viewChatUserList(Long roomId){
        // 채팅방-채팅유저 조회 (num, name, pos, dep)
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("채팅방이 없습니다."));
        List<ChatUser> chatUsers = chatUserRepository.findAllByChatRoom(chatRoom);
        return chatUsers.stream().map(ChatUser::fromEntity).toList();
    }

    // 채팅방 구성원 초대
    // chatRoomId(chatRoom)과 초대하려는 userNum(user)을 chatUser에 저장.
    // ⭐⭐⭐ 새로 들어왔다는 메세지 chatRoom 에 전송.
    public String inviteChatUser(String inviteUserNum, Long roomId){
        // 채팅 유저 초대
        User user = userRepository.findByUserNum(inviteUserNum).orElseThrow(()->new EntityNotFoundException("없는 사원입니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("채팅방이 없습니다."));
        ChatUser chatUser = ChatUser.toEntity(chatRoom, user);
        chatUserRepository.save(chatUser);

        // ⭐⭐⭐ oo님이 입장하셨습니다. 메세지 발송? 아님 이것만 알림?

        return chatUser.getUser().getUserNum();
    }

}
