package com.example.exodia.chat.domain;

import com.example.exodia.chat.dto.ChatUserResponse;
import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import jakarta.persistence.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "del_yn = 'N'")
public class ChatUser extends BaseTimeEntity {

    // 채팅유저 : 채팅방과 유저 id 포함

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 채팅유저고유의 id

    //    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    //    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 사번

    private int unreadChatNum;

    public static ChatUser toEntity(ChatRoom chatRoom, User user){ // chatRoom 생성시 사용
        return ChatUser.builder()
                .chatRoom(chatRoom)
                .user(user)
//                .unreadChat(0)
                .build();
    }

    public ChatUserResponse fromEntity(){
        return ChatUserResponse.builder()
                .chatUserNum(this.getUser().getUserNum())
                .chatUserName(this.getUser().getName())
                .chatUserPosName(this.getUser().getPosition().getName())
                .chatUserDepName(this.getUser().getDepartment().getName())
                .build();
    }
}