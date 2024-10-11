package com.example.exodia.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatFileRequest { // presigned url 만들 때 받는 값
    private String chatFileName;
    private long fileSize; // 최대 용량 100MB
}
