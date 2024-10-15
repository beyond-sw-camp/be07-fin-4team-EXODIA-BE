package com.example.exodia.board.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tags")
public class Tags {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "board_id")
    @JsonIgnore
    private Board board;

    private String tag;

    /**
     * 태그 생성 메서드
     * @param board 게시물 정보
     * @param tag 태그명
     * @return 생성된 Tags 객체
     */
    public static Tags createTag(Board board, String tag) {
        return Tags.builder()
                .board(board)
                .tag(tag)
                .build();
    }

    /**
     * 태그 수정 메서드
     * @param newTag 수정할 태그명
     */
    public void updateTag(String newTag) {
        this.tag = newTag;
    }
}
