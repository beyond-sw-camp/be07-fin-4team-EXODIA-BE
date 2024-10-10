// QnASaveReqDto.java
package com.example.exodia.qna.dto;


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
    private Long departmentId;  // department 객체 대신 departmentId 필드 사용
    @Builder.Default
    private DelYN delYN = DelYN.N;
    @Builder.Default
    private Boolean anonymous = false;
    private List<MultipartFile> files;

    // DTO를 Entity로 변환하는 메서드 (Department는 서비스에서 직접 할당)
    public QnA toEntity(User user, Department department) {
        return QnA.builder()
                .questioner(user)
                .title(this.title)
                .questionText(this.questionText)
                .anonymous(this.anonymous)
                .delYN(this.delYN)
                .department(department)
                .build();
    }
}
