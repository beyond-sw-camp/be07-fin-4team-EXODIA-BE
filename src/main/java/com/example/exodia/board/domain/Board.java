package com.example.exodia.board.domain;

import com.example.exodia.board.dto.BoardDetailDto;
import com.example.exodia.board.dto.BoardListResDto;
import com.example.exodia.comment.domain.Comment;
import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "board")
@Where(clause = "del_yn = 'N'")
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    private Long hits = 0L;

    // 작성자 정보 (익명 게시글의 경우 null, 수정 필요)
    @ManyToOne
    @JoinColumn(name = "user_num", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    private DelYN delYn = DelYN.N;

    @Builder.Default
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tags> tags = new ArrayList<>();

    /**
     * 게시물 목록 DTO로 변환
     * @return BoardListResDto
     */
    public BoardListResDto listFromEntity() {
        // 태그 목록을 String 리스트로 변환
        List<String> tagList = this.tags.stream()
                .map(Tags::getTag) // 태그 문자열 추출
                .collect(Collectors.toList());

        return BoardListResDto.builder()
                .id(this.id)
                .title(this.title)
                .category(category)
                .hits(this.hits)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .isPinned(this.isPinned)
                .user_num(user.getUserNum())
                .tags(tagList) // 태그 리스트 추가
                .build();
    }

    /**
     * 게시물 상세 DTO로 변환
     * @return BoardDetailDto
     */
    public BoardDetailDto detailFromEntity(List<BoardFile> files) {
        // 태그 목록을 String 리스트로 변환
        List<String> tagList = this.getTags().stream()
                .map(tags -> tags.getTag()) // 태그 객체에서 태그 문자열을 추출
                .collect(Collectors.toList());

        return BoardDetailDto.builder()
                .id(this.getId())
                .title(this.getTitle())
                .content(this.getContent())
                .category(category)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .files(files) // Board 객체에 포함된 파일 리스트 사용
                .hits(this.hits)
                .user_num(user.getUserNum())
                .tags(tagList) // 변환된 태그 리스트 추가
                .build();
    }

    /**
     * 조회수를 업데이트하는 메서드
     * @param hits Redis에서 조회된 조회수
     */
    public void updateBoardHitsFromRedis(Long hits) {
        this.hits = hits;
    }
}
