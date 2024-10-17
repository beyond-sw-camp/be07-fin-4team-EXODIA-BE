package com.example.exodia.chat.service;

import com.example.exodia.chat.domain.*;
import com.example.exodia.chat.dto.ChatAlarmResponse;
import com.example.exodia.chat.dto.ChatFileMetaDataResponse;
import com.example.exodia.chat.dto.ChatMessageRequest;
import com.example.exodia.chat.dto.ChatMessageResponse;
import com.example.exodia.chat.repository.ChatMessageRepository;
import com.example.exodia.chat.repository.ChatRoomRepository;
import com.example.exodia.chat.repository.ChatUserRepository;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.common.service.SseEmitters;
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

import java.util.ArrayList;
import java.util.List;

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
    private final ChatUserRepository chatUserRepository;

    private final SseEmitters sseEmitters;

    @Autowired
    public ChatMessageService(ChatRoomManage chatRoomManage, @Qualifier("chat") RedisTemplate<String, Object> chatredisTemplate, @Qualifier("chat") ChannelTopic channelTopic,
                              ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository,
                              UserRepository userRepository, FileUploadService fileUploadService,
                              ChatUserRepository chatUserRepository, SseEmitters sseEmitters) {
        this.chatRoomManage = chatRoomManage;
        this.chatredisTemplate = chatredisTemplate;
        this.channelTopic = channelTopic;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
        this.fileUploadService = fileUploadService;
        this.chatUserRepository = chatUserRepository;
        this.sseEmitters = sseEmitters;
    }

    // 채팅방에 메세지 발송
    @Transactional
    public void sendMessage(ChatMessageRequest chatMessageRequest){
        // 보낸 사람
        User user = userRepository.findByUserNumAndDelYn(chatMessageRequest.getSenderNum(), DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 사원입니다"));
        // 채팅방
        ChatRoom chatRoom = chatRoomRepository.findById(chatMessageRequest.getRoomId()).orElseThrow(()->new EntityNotFoundException("없는 채팅방입니다."));
        // 채팅 참여자.
        List<ChatUser> chatUsers = chatUserRepository.findAllByChatRoom(chatRoom);

        for(ChatUser receiver : chatUsers){
            String receiverNum = receiver.getUser().getUserNum();
            if(receiverNum.equals(user.getUserNum())){
                continue;
            }
            // receiver 채팅방에 있는 지 확인 // 알림과 unread 메세지 관리
            String receiverChatRoomId = chatRoomManage.getChatroomIdByUser(receiverNum);
            String key = "chatRoom_" + chatRoom.getId() + "_" + receiverNum;
            String alarmKey = "user_alarm_" + receiverNum;
            // receiver 채팅방에 있다면
            if(receiverChatRoomId != null && receiverChatRoomId.equals(Long.toString(chatRoom.getId()))){
                chatredisTemplate.opsForValue().set(key, "0");
            }else { // receiver 채팅방에 없다면
                // ⭐⭐⭐ 알림 내용 : 어느방의 누가 무엇을 보냈나.
                // chatAlarm 개수 + 1
                // 해당 chatRoom 입장시 -> chatRoom의 unread = 0 // chatAlarm개수 - 해당 chatRoom unreadChat 개수

                if(chatMessageRequest.getMessageType() == MessageType.FILE){
                    sseEmitters.sendChatToUser(receiverNum, ChatAlarmResponse.builder()
                            .senderName(user.getName())
                            .roomName(chatRoom.getRoomName())
                            .message("FILE 전송")
                            .build());
                }else{
                    sseEmitters.sendChatToUser(receiverNum, ChatAlarmResponse.builder()
                            .senderName(user.getName())
                            .roomName(chatRoom.getRoomName())
                            .message(chatMessageRequest.getMessage())
                            .build());
                }
                String temp = chatRoomManage.getChatAlarm(receiverNum);
                int alarm = 0;
                if(temp != null){
                    alarm = Integer.parseInt(temp);
                }
                chatRoomManage.updateChatAlarm(receiverNum, alarm+1);

                Object obj = chatredisTemplate.opsForValue().get(key);
                if(obj != null){ // unread 메세지가 있다면
                    try {
                        String s =(String) obj;
                        int num = Integer.parseInt(s);
                        chatredisTemplate.opsForValue().set(key, Integer.toString(num+1));
                    }catch (Exception e){
                        log.error(e.getMessage());
                    }
                }else { // unread 메세지가 없다면
                    chatredisTemplate.opsForValue().set(key, "1");
                }
            }
        }

        // 채팅 메세지 db 저장
        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessageRequest.toEntity(user, chatRoom)); // chatFile new array 만든다..!
        // chatRoom 에 최신메세지와 시간 저장. -> 목록 조회시 사용
        chatRoom.updateRecentChat(savedChatMessage);

        // 파일을 포함한 메세지일 경우, 파일메타데이터 db에 저장
        // 전송할 messageRes 조정
        ChatMessageResponse chatMessageResponse = new ChatMessageResponse();
        if(chatMessageRequest.getMessageType() == MessageType.FILE){
            List<ChatFile> chatFileList = fileUploadService.saveChatFileMetaData(savedChatMessage, chatMessageRequest.getFiles());
            for(ChatFile cf : chatFileList){
                savedChatMessage.getChatFiles().add(cf);
            }
//            savedChatMessage.setChatFiles(fileUploadService.saveChatFileMetaData(savedChatMessage, chatMessageRequest.getFiles())); // 전송 온 파일들 chatFile 저장

            chatMessageResponse = savedChatMessage.fromEntityWithFile();
            List<ChatFileMetaDataResponse> files = savedChatMessage.getChatFiles().stream().map(ChatFile::fromEntity).toList();
            for(ChatFileMetaDataResponse cfmd : files){
                chatMessageResponse.getFiles().add(cfmd);
            }
        }else{
            chatMessageResponse = savedChatMessage.fromEntity();
        }

        // publish
        chatredisTemplate.convertAndSend(channelTopic.getTopic(), chatMessageResponse);
    }
}
