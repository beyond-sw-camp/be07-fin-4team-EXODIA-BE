package com.example.exodia.qna.dto;

import com.example.exodia.board.domain.BoardFile;
import com.example.exodia.comment.dto.CommentResDto;
import com.example.exodia.qna.domain.QnA;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QnADetailDto {
    private Long id;
    private String title;
    private String questionText;
    private String answerText;
    private String questionUserName;
    private String answerUserName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime answeredAt;
    private List<BoardFile> qFiles;
    private List<BoardFile> aFiles;
    private List<CommentResDto> comments;

    public static QnADetailDto fromEntity(QnA qna, List<CommentResDto> comments) {
        return QnADetailDto.builder()
                .id(qna.getId())
                .title(qna.getTitle())
                .questionText(qna.getQuestionText())
                .answerText(qna.getAnswerText())
                .questionUserName(qna.getQuestioner().getName()) // 질문자 이름
                .answerUserName(qna.getAnswerer() != null ? qna.getAnswerer().getName() : null) // 답변자가 없을 수 있으므로 null 체크
                .createdAt(qna.getCreatedAt())
                .updatedAt(qna.getUpdatedAt())
                .answeredAt(qna.getAnsweredAt())
                .qFiles(qna.getQuestionerFiles() != null ? qna.getQuestionerFiles() : List.of()) // 질문자 파일 리스트 null 체크
                .aFiles(qna.getAnswererFiles() != null ? qna.getAnswererFiles() : List.of()) // 답변자 파일 리스트 null 체크
                .comments(comments != null ? comments : List.of()) // 댓글 리스트 null 체크
                .build();
    }

}
