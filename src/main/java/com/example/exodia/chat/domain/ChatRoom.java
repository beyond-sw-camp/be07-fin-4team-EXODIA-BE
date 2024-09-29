package com.example.exodia.chat.domain;

import com.example.exodia.chat.dto.*;
import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;
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

    @Column(nullable = false)
    private String roomName;

//    private Integer userCount; // 채팅방 인원수

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatUser> chatUsers = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    public static ChatRoom toEntity(ChatRoomRequest chatRoomRequest){
        return ChatRoom.builder()
                .roomName(chatRoomRequest.getRoomName())
                .build();
    }

    public ChatRoomResponse fromEntity (List<String> userNums){
        return ChatRoomResponse.builder()
                .roomId(this.getId())
                .roomName(this.getRoomName())
                .userNums(userNums)
                .build();
    }

    public ChatRoomExistResponse fromEntityExistChatRoom(boolean check){
        List<String> userNums = this.getChatUsers().stream().map(p->p.getUser().getUserNum()).collect(Collectors.toList());

        return ChatRoomExistResponse.builder()
                .existCheck(check)
                .roomId(this.getId())
                .roomName(this.getRoomName())
                .userNums(userNums)
                .build();
    }

    public ChatRoomSimpleResponse fromEntitySimpleChatRoom (){
        return ChatRoomSimpleResponse.builder()
                .roomId(this.getId())
                .roomName(this.getRoomName())
                .userNumbers(this.getChatUsers().size())
                .build();
    }
}