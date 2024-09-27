package com.example.exodia.chat.domain;

import com.example.exodia.chat.dto.ChatMessageRequest;
import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.user.domain.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "del_yn = 'N'")
public class ChatMessage extends BaseTimeEntity {

    // 채팅메세지 : 채팅방id와 유저id를 포함한 채팅유저를 가진다.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 채팅메세지고유의 id

    //    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @ManyToOne
    @JoinColumn(name = "chat_user_id", nullable = false)
    private User chatUser; // 누가 보냈는지 // ChatUser말고 그냥 멤버를 참조할까.. 채팅방정보가 중복된다.

    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom; // 어느 방의 채팅인지

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    private String message;

    @OneToOne
    @JoinColumn(name = "chat_file_id") // file이 아니면 null
    private ChatFile chatFile; // 파일을 보내는 순간 먼저 생겨서 참조되어야한다.

    @Column(name = "send_at", updatable = false, nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm", timezone = "Asia/Seoul")
    private LocalDateTime sendAt;


    public static ChatMessage toEntity(User user, ChatRoom chatRoom, ChatMessageRequest chatMessageRequest){
        return ChatMessage.builder()
                .chatUser(user)
                .chatRoom(chatRoom)
                .messageType(chatMessageRequest.getMessageType())
                .message(chatMessageRequest.getMessage())
                .sendAt(chatMessageRequest.getSendAt())
                .build();
    }

    public ChatMessage toEntityWithFile(User user, ChatRoom chatRoom, ChatMessageRequest chatMessageRequest){
        return ChatMessage.builder()
                .chatUser(user)
                .chatRoom(chatRoom)
                .messageType(chatMessageRequest.getMessageType())
                .message(chatMessageRequest.getMessage())
                .sendAt(chatMessageRequest.getSendAt())
                .build();
    }
}