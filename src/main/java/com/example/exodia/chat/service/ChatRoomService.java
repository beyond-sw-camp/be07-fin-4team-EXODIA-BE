package com.example.exodia.chat.service;

import com.example.exodia.chat.domain.ChatMessage;
import com.example.exodia.chat.domain.ChatRoom;
import com.example.exodia.chat.domain.ChatUser;
import com.example.exodia.chat.dto.*;
import com.example.exodia.chat.repository.ChatMessageRepository;
import com.example.exodia.chat.repository.ChatRoomRepository;
import com.example.exodia.chat.repository.ChatUserRepository;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.common.service.KafkaProducer;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ChatRoomService {

    private final ChatRoomManage chatRoomManage; // redis로 채팅룸 입장유저들 관리 // 채팅 알림 (unread 총합)
    // stateKey "user_" + userNum , chatRommId
    // alarmKey "user_alarm_" + userNum , chatUnreadTotal(alarm)

    @Qualifier("chat") // 메세지 pubsub // 각 chatRoom + user의 unread 메세지 개수 관리
    private final RedisTemplate<String, Object> chatredisTemplate;
    // unreadKey "chatRoom_" + chatRoom.getId() + "_" + userNum , unread 개수

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;
    private final UserRepository userRepository;

    private final KafkaProducer kafkaProducer;

    @Autowired
    public ChatRoomService(ChatRoomManage chatRoomManage, ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository,
                           ChatUserRepository chatUserRepository, UserRepository userRepository,
                           @Qualifier("chat") RedisTemplate<String, Object> chatredisTemplate, KafkaProducer kafkaProducer) {
        this.chatRoomManage = chatRoomManage;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatUserRepository = chatUserRepository;
        this.userRepository = userRepository;
        this.chatredisTemplate = chatredisTemplate;
        this.kafkaProducer = kafkaProducer;
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

    // 채팅방 생성 (생성 이후 채팅방목록으로 넘어감.)
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
            String unreadKey = "chatRoom_" + chatRoom.getId() + "_" + userNum;
            String unread = (String)chatredisTemplate.opsForValue().get(unreadKey);
            int unreadChat = 0;
            if(unread != null){
                unreadChat = Integer.parseInt(unread);
            }
            chatRoomResponses.add(chatRoom.fromEntity(unreadChat));
        }
        return chatRoomResponses; // id, name, usernums, unreadchatnum, recentChat + ⭐ recentchatTime
    }


      // 채팅방 list에서 검색. 채팅방명, 채팅user이름
    // ⭐ 검색어 치면서 검색 결과가 나오면 참 좋을텐데
    public List<ChatRoomResponse> searchChatRoom(String userNum, String searchValue){
        // 채팅방 목록 조회하는 유저 확인
        User user = userRepository.findByUserNum(userNum).orElseThrow(()->new EntityNotFoundException("없는 사원입니다."));
        if(searchValue == null || searchValue.isEmpty()){
            return viewChatRoomList(userNum);
        }
        List<ChatUser> chatUsers = chatUserRepository.findAllByUser(user);
        List<ChatRoom> chatRooms = chatUsers.stream().map(ChatUser::getChatRoom).toList();
        chatRooms = chatRooms.stream().sorted(Comparator.comparing(ChatRoom::getRecentChatTime).reversed()).toList();
        List<ChatRoomResponse> chatRoomResponseList = new ArrayList<>();
        for(ChatRoom chatRoom : chatRooms){
            String unreadKey = "chatRoom_" + chatRoom.getId() + "_" + userNum;
            String unreadNum = (String)chatredisTemplate.opsForValue().get(unreadKey);
            int unreadChat = 0;
            if(unreadNum != null){
                unreadChat = Integer.parseInt(unreadNum);
            }
            if(chatRoom.getRoomName().equals(searchValue)){
                chatRoomResponseList.add(chatRoom.fromEntity(unreadChat));
            }
            for(ChatUser chatUser : chatRoom.getChatUsers()){
                if(chatUser.getUser().getName().equals(searchValue)){
                    chatRoomResponseList.add(chatRoom.fromEntity(unreadChat));
                }
            }
        }
        return chatRoomResponseList;
    }

    public int getChatAlarm(){
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        String alarmNum = chatRoomManage.getChatAlarm(userNum);
        int alarm = 0; // ⭐⭐⭐
        if(alarmNum!=null){
            alarm = Integer.parseInt(alarmNum);
        }
        return alarm;
    }


    // 채팅방 메세지 조회 == 채팅방 입장
    public List<ChatMessageResponse> viewChatMessageList(Long roomId){
        // chatMessageList를 불러온다 == chatRoom에 입장한다. 1. chatRoomManage(redis로 관리) user의 현 채팅방id 기록
        // chatMessageList를 불러온다 == 입장 유저가 확인하지 않은 채팅을 읽는다. // 3. 채팅방의 unread 메세지 삭제.
                                                                         // 2. chatAlarm개수에서 해당 chatRoom unread 개수 뺀다.
        // 1
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        chatRoomManage.updateChatRoomId(userNum, roomId);
        // 2 ⭐⭐⭐ header sse : 뭔가 이상
        String unreadKey = "chatRoom_" + roomId + "_" + userNum;
        String unreadNum = (String) chatredisTemplate.opsForValue().get(unreadKey);
        String alarmNum = chatRoomManage.getChatAlarm(userNum);
        int alarm = 0; // ⭐⭐⭐
        if(alarmNum!=null){
            alarm = Integer.parseInt(alarmNum);
        }
        if(unreadNum!=null && alarmNum!=null){
            alarm = Integer.parseInt(alarmNum) - Integer.parseInt(unreadNum);
            chatRoomManage.updateChatAlarm(userNum, alarm);
        }
        // Kafka 이벤트 전송
        String message = "1" + "|" + "2" + "|" + "3" + "|" + "4" + "|" + alarm;
        kafkaProducer.enterChatAlarmEvent(userNum,message);
        // 3
        chatredisTemplate.delete(unreadKey);
        return chatMessageRepository.findAllByChatRoomId(roomId)
                .stream().map(ChatMessage::fromEntityForChatList).collect(Collectors.toList());
    }

    // 채팅방 퇴장
    public String exitChatRoom(String userNum){
        // chatRoom을 나올 때 chatRoomManage(redis로 관리) user의 현 채팅방id 기록 삭제
        User user = userRepository.findByUserNum(userNum).orElseThrow(()->new EntityNotFoundException("없는 사원입니다."));
        chatRoomManage.exitChatRoom(userNum);
        return userNum;
    }

    // 채팅방 삭제 == 나가려는 chatRoomId(chatRoom)의 userNum(User)을 가진 chatUser 삭제.
    public ChatUserInfoResponse hardExitChatRoom(String userNum, Long roomId){
        User user = userRepository.findByUserNum(userNum).orElseThrow(()->new EntityNotFoundException("없는 사원입니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("채팅방이 없습니다."));
        List<ChatUser> chatUsers = chatUserRepository.findAllByChatRoom(chatRoom);
        if(chatUsers.isEmpty()){ // 채팅방에 참여한 chatUser가 없다. -> 채팅방 삭제
            chatRoom.softDelete();
        }else{
            ChatUser chatUser = chatUserRepository.findByUserAndChatRoom(user, chatRoom).orElseThrow(()->new EntityNotFoundException("채팅 유저가 없습니다."));
            if(!chatUser.isDeleted()){
//                chatUserRepository.delete(chatUser); // 삭제
                chatUser.softDelete();
                System.out.println(chatUser.getId());

                // chatRoom을 나올 때 chatRoomManage(redis로 관리) user의 현 채팅방id 기록 삭제
                chatRoomManage.exitChatRoom(userNum);
                // 채팅방의 unread 메세지 삭제
                String unreadKey = "chatRoom_" + roomId + "_" + userNum;
                chatredisTemplate.delete(unreadKey);
            }
        }
        return ChatUserInfoResponse.builder().senderName(user.getName()).senderNum(user.getUserNum()).build();
    }

    // 채팅방 구성원 조회
    public List<ChatUserResponse> viewChatUserList(Long roomId){
        // 채팅방-채팅유저 조회 (num, name, pos, dep)
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("채팅방이 없습니다."));
        List<ChatUser> chatUsers = chatUserRepository.findAllByChatRoom(chatRoom);
        return chatUsers.stream().map(ChatUser::fromEntity).toList();
    }

    // 채팅방 구성원 초대
    public ChatUserInfoResponse inviteChatUser(String inviteUserNum, Long roomId){
        // chatRoomId(chatRoom)과 초대하려는 userNum(user)을 chatUser에 저장.
        User user = userRepository.findByUserNum(inviteUserNum).orElseThrow(()->new EntityNotFoundException("없는 사원입니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("채팅방이 없습니다."));
        ChatUser chatUser = ChatUser.toEntity(chatRoom, user);
        chatUserRepository.save(chatUser);
        chatRoom.setChatUsers(chatUser);
        // ⭐⭐ 초대된 유저에게 알림?
        return ChatUserInfoResponse.builder().senderName(user.getName()).senderNum(user.getUserNum()).build();
    }

}
