package com.example.exodia.chat.domain;

import com.example.exodia.chat.dto.ChatFileMetaDataResponse;
import com.example.exodia.chat.dto.ChatMessageResponse;
import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import jakarta.persistence.*;// 얘 있음 안된다.

import java.util.ArrayList;
import java.util.List;

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
    private User chatUser; // 누가 보냈는지

    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom; // 어느 방의 채팅인지

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    private String message;

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL)
    private List<ChatFile> chatFiles;

    public ChatMessageResponse fromEntity(){ // 저장하고, 바로 보낼 때
        return ChatMessageResponse.builder()
                .senderNum(this.getChatUser().getUserNum())
                .senderName(this.getChatUser().getName())
                .senderDepName(this.getChatUser().getDepartment().getName())
                .senderPosName(this.getChatUser().getPosition().getName())
                .roomId(this.getChatRoom().getId())
                .messageType(this.getMessageType())
                .message(this.getMessage())
                .files(new ArrayList<>())
                .createAt(this.getCreatedAt().toString())
                .build();
    } // () ? : "알수없음"


    public ChatMessageResponse fromEntityWithFile(){ // 저장하고, 바로 보낼 때
        return ChatMessageResponse.builder()
                .senderNum(this.getChatUser().getUserNum())
                .senderName(this.getChatUser().getName())
                .senderDepName(this.getChatUser().getDepartment().getName())
                .senderPosName(this.getChatUser().getPosition().getName())
                .roomId(this.getChatRoom().getId())
                .messageType(this.getMessageType())
                .message(this.getMessage())
                .files(new ArrayList<>())
                .createAt(this.getCreatedAt().toString())
                .build();
    }

    public ChatMessageResponse fromEntityForChatList(){ // list 만들때.
        return ChatMessageResponse.builder()
                .senderNum(this.getChatUser().getUserNum())
                .senderName(this.getChatUser().getName())
                .senderDepName(this.getChatUser().getDepartment().getName())
                .senderPosName(this.getChatUser().getPosition().getName())
                .roomId(this.getChatRoom().getId())
                .messageType(this.getMessageType())
                .message(this.getMessage())
                .files(new ArrayList<>(this.getChatFiles().stream().map(ChatFile::fromEntity).toList()))
                .createAt(this.getCreatedAt().toString())
                .build();
    }
}