package com.example.exodia.chat.domain;

import com.example.exodia.chat.dto.ChatFileMetaDataResponse;
import com.example.exodia.chat.dto.ChatFileSaveListDto;
import com.example.exodia.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "del_yn = 'N'")
public class ChatFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 채팅파일고유의 id

    @ManyToOne
    @JoinColumn(name = "chat_message_id", nullable = false)
    private ChatMessage chatMessage;

    private String chatFileName; // 파일이름

    private String chatFileUrl; // 파일 업로드 url

    private String chatFileDir; // s3 파일 경로 // 이부분은 좀 더 고려

    public ChatFileMetaDataResponse fromEntity (){
        return ChatFileMetaDataResponse.builder()
                .id(this.getId())
                .chatFileName(this.getChatFileName())
                .chatFileUrl(this.getChatFileUrl())
                .build();
    }

}
