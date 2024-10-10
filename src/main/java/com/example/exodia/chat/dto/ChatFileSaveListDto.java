package com.example.exodia.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatFileSaveListDto { // front에서 presigned url 로 업로드 이후 // chatMessageRequest의 file list 구성 항목
    private String chatFileName;
    private String chatFileUrl;

}
