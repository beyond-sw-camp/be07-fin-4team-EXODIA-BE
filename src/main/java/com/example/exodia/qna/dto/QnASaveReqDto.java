// QnASaveReqDto.java
package com.example.exodia.qna.dto;


import com.example.exodia.board.domain.BoardFile;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.department.domain.Department;

import com.example.exodia.qna.domain.QnA;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QnASaveReqDto {
    private String title;
    private String questionText;
    private User questioner;
    @Builder.Default
    private DelYN delYN = DelYN.N;
    @Builder.Default
    private Boolean secretBoard = false;
    @Builder.Default
    private Boolean anonymous = false;
    private List<MultipartFile> files;
    private Department department;

    // DTO를 Entity로 변환하는 메서드
    public QnA toEntity(User user,Department department) {
        QnA qna = QnA.builder()
                .questioner(user)
                .title(this.title)
                .questionText(this.questionText)
                .secretBoard(this.secretBoard)
                .anonymous(this.anonymous)
                .delYN(this.delYN)
                .department(department)
                .build();

        return qna;
    }
}
