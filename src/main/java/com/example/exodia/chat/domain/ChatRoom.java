package com.example.exodia.chat.domain;

import com.example.exodia.chat.dto.*;
import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "del_yn = 'N'")
public class ChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 채팅방 고유의 id

    private String roomName;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatUser> chatUsers;

    private String recentChat;
    private LocalDateTime recentChatTime;

    public void updateRecentChat(ChatMessage chatMessage){
        this.recentChat = chatMessage.getMessage();
        this.recentChatTime = chatMessage.getCreatedAt();
        if(chatMessage.getMessageType() == MessageType.FILE){
            this.recentChat = "file 전송";
        }
    }

    public void setChatUser(ChatUser chatUser){
        this.chatUsers.add(chatUser);
    }

    public void deleteChatUser(ChatUser chatUser){
        this.chatUsers.remove(chatUser);
        System.out.println("나가면 한명만 남는데 왜 empty값이 들어가 이 자식아.");
        System.out.println(this.chatUsers.size());
    }

    public void updateChatRoomName(String chatRoomName){
        this.roomName = chatRoomName;
    }

    public ChatRoomResponse fromEntity (int unreadChat){ // 단일 조회 , 목록 조회
        List<ChatRoomUserResponse> userList = this.getChatUsers().stream().map(ChatUser::fromEntityForRoomList).toList();
        System.out.println("empty값은 대체 뭐야");
        System.out.println(userList.size());
        return ChatRoomResponse.builder()
                .roomId(this.getId())
                .roomName(this.getRoomName())
                .users(userList)
                .recentChat(this.getRecentChat()!=null ? this.getRecentChat():"")
                .unreadChatNum(unreadChat)
                .recentChatTime(this.getRecentChat()!= null ? this.getRecentChatTime().toString() : "")
                .build();
    }

    public ChatRoomExistResponse fromEntityExistChatRoom(boolean check){ // 생성 결과
        List<String> userNums = this.getChatUsers().stream().map(p->p.getUser().getUserNum()).collect(Collectors.toList());

        return ChatRoomExistResponse.builder()
                .existCheck(check)
                .roomId(this.getId())
                .roomName(this.getRoomName())
                .userNums(userNums)
                .build();
    }
}