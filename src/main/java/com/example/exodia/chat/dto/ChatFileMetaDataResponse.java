package com.example.exodia.chat.dto;

import com.example.exodia.chat.domain.ChatFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatFileMetaDataResponse { // chatMessageResponse 에 들어가는 file list의 항목

    private Long id;
    private String chatFileName;
    private String chatFileUrl;

    public static ChatFileMetaDataResponse fromEntity(ChatFile chatFile){
        return ChatFileMetaDataResponse.builder()
                .id(chatFile.getId())
                .chatFileName(chatFile.getChatFileName())
                .chatFileUrl(chatFile.getChatFileUrl())
                .build();
    }

}
