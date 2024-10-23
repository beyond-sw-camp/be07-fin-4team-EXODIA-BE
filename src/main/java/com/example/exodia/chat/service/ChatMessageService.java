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
import com.example.exodia.common.service.KafkaProducer;
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

import java.util.List;

@Service
@Slf4j
public class ChatMessageService {
    private final ChatRoomManage chatRoomManage; // redis로 채팅룸 입장유저들 관리 // 채팅 알림 (unread 총합)
    // stateKey "user_" + userNum , chatRommId
    // alarmKey "user_alarm_" + userNum , chatUnreadTotal(alarm)

    @Qualifier("chat") // 메세지 pubsub // 각 chatRoom + user의 unread 메세지 개수 관리
    private final RedisTemplate<String, Object> chatredisTemplate;
    // unreadKey "chatRoom_" + chatRoom.getId() + "_" + userNum , unread 개수

    @Qualifier("chat")
    private final ChannelTopic channelTopic;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final ChatUserRepository chatUserRepository;

    private final KafkaProducer kafkaProducer;

    @Autowired
    public ChatMessageService(ChatRoomManage chatRoomManage, @Qualifier("chat") RedisTemplate<String, Object> chatredisTemplate, @Qualifier("chat") ChannelTopic channelTopic,
                              ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository,
                              UserRepository userRepository, FileUploadService fileUploadService,
                              ChatUserRepository chatUserRepository, KafkaProducer kafkaProducer) {
        this.chatRoomManage = chatRoomManage;
        this.chatredisTemplate = chatredisTemplate;
        this.channelTopic = channelTopic;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
        this.fileUploadService = fileUploadService;
        this.chatUserRepository = chatUserRepository;
        this.kafkaProducer = kafkaProducer;
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

        // 채팅 메세지 db 저장
        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessageRequest.toEntity(user, chatRoom));
        // chatRoom 에 최신메세지와 시간 저장. -> 목록 조회시 사용
        chatRoom.updateRecentChat(savedChatMessage);

        // 알림
        for(ChatUser receiver : chatUsers){
            String receiverNum = receiver.getUser().getUserNum();
            if(receiverNum.equals(user.getUserNum())){ // sender 외의 채팅유저들에게
                continue;
            }
            String receiverChatRoomId = chatRoomManage.getChatroomIdByUser(receiverNum);
            String unreadKey = "chatRoom_" + chatRoom.getId() + "_" + receiverNum;
            if(receiverChatRoomId != null && receiverChatRoomId.equals(Long.toString(chatRoom.getId()))){ // receiver 채팅방에 있다면
                System.out.println("채팅방에 있다.");
            }else { // receiver 채팅방에 없다면
                // 알림 // ⭐⭐⭐ 접속이 끊겨 있을 때에도 모여야한다. => chatUser(user-chatRoom)에 unread값을 기록. chatAlarm은 unread값들의 합.
                // 1-1 (redis)chatAlarm 개수 + 1 / header kafka-sse : 어느방의 누가 무엇을 보냈나.
                // 1-2 그 외 해당 채팅방 (redis)unread + 1 // 1-3 해당 채팅방 (db)recent 관련 업데이트(위에서)
                // 2. 해당 유저 chatList sse : 채팅방 리스트 reload

                // 1-1
                String alarmNum = chatRoomManage.getChatAlarm(receiverNum);
                int alarm = 0;
                if(alarmNum != null){
                    alarm = Integer.parseInt(alarmNum);
                }
                alarm+=1;
                chatRoomManage.updateChatAlarm(receiverNum, alarm);
                // Kafka 이벤트 전송 // 일단은 알람개수만 쓴다.
                String message = user.getName() + "|" + chatRoom.getRoomName() + "|" + savedChatMessage.getMessageType().toString() + "|" + savedChatMessage.getMessage() + "|" + alarm;
                kafkaProducer.sendChatAlarmEvent(receiverNum, message);

                // 1-2
                Object obj = chatredisTemplate.opsForValue().get(unreadKey);
                if(obj != null){ // unread 메세지가 있다면
                    try {
                        String s =(String) obj;
                        int num = Integer.parseInt(s);
                        chatredisTemplate.opsForValue().set(unreadKey, Integer.toString(num+1));
                    }catch (Exception e){
                        log.error(e.getMessage());
                    }
                }else { // unread 메세지가 없다면
                    chatredisTemplate.opsForValue().set(unreadKey, "1");
                }

                // 2 // ⭐ 채팅방이 켜있든, 켜있지 않든?
                String temp = "1" + "|" + "2" + "|" + "3" + "|" + "4" + "|" + "5";
                kafkaProducer.chatRoomListUpdateEvent(receiverNum, temp);

            }
        }

        // 파일을 포함한 메세지일 경우, 파일메타데이터 db에 저장
        // 전송할 messageRes 조정
        ChatMessageResponse chatMessageResponse = new ChatMessageResponse();
        if(chatMessageRequest.getMessageType() == MessageType.FILE){
            List<ChatFile> chatFileList = fileUploadService.saveChatFileMetaData(savedChatMessage, chatMessageRequest.getFiles());
            for(ChatFile cf : chatFileList){
                savedChatMessage.getChatFiles().add(cf);
            }
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
