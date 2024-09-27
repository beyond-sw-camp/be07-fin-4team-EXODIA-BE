package com.example.exodia.qna.dto;


import com.example.exodia.common.domain.DelYN;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QnAAnswerReqDto {

    private String answererName;
    private String answerText;
    private LocalDateTime answeredAt;
    @Builder.Default
    private DelYN delYN = DelYN.N;
    private List<MultipartFile> files;

}
