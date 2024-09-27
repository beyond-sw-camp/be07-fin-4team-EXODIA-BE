package com.example.exodia.qna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QnAAtoUpdateDto {
    private String answererName;
    private String answerText;
    private LocalDateTime updatedAt;
    private List<MultipartFile> files;
}

