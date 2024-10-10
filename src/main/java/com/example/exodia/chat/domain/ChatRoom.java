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

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatUser> chatUsers = new ArrayList<>();


    public ChatRoomResponse fromEntity (){ // 단일 조회 , 목록 조회
        List<String> userNums = this.getChatUsers().stream().map(p->p.getUser().getUserNum()).collect(Collectors.toList());

        return ChatRoomResponse.builder()
                .roomId(this.getId())
                .roomName(this.getRoomName())
                .userNums(userNums)
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