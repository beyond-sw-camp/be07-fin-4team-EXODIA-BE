package com.example.exodia.comment.dto;

import com.example.exodia.board.domain.Board;
import com.example.exodia.comment.domain.Comment;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.qna.domain.QnA;
import com.example.exodia.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommentSaveReqDto {

    private Long board_id;
    private Long question_id;
    private String userNum;
    private String content;
    private String name;

    @Builder.Default
    private DelYN delYn = DelYN.N; // 기본값을 N으로 설정

    public Comment BoardToEntity(User user, Board board, String userNum) {
        return Comment.builder()
                .content(this.content)
                .delYn(this.delYn)
                .user(user)
                .name(user.getName())
                .userNum(userNum)
                .board(board)
                .build();
    }

    public Comment QnaToEntity(User user, QnA qna, String userNum) {
        return Comment.builder()
                .content(this.content)
                .delYn(this.delYn)
                .user(user)
                .name(user.getName())
                .userNum(userNum)
                .qna(qna)
                .build();
    }
}
