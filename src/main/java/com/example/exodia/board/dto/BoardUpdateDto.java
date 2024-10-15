package com.example.exodia.board.dto;

import com.example.exodia.board.domain.Board;
import com.example.exodia.board.domain.Category;
import com.example.exodia.board.domain.Tags;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.user.domain.User;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardUpdateDto {

    private String title;
    private String content;
    private List<MultipartFile> files;
    private boolean isPinned;
    private Category category;
    private User user;
    private List<String> tags; // 태그를 List<String>으로 변경
    @Builder.Default
    private DelYN delYn = DelYN.N;

    /**
     * Board 엔티티 업데이트 메서드.
     * 기존 Board 엔티티에 새로운 태그와 기타 필드를 업데이트합니다.
     *
     * @param category 게시물 카테고리
     * @param user 게시물 작성자(User 객체)
     * @return 업데이트된 Board 엔티티
     */
    public Board updateFromEntity(Category category, User user) {
        List<Tags> tagList = this.tags.stream()
                .map(tag -> Tags.builder().tag(tag.trim()).build())
                .collect(Collectors.toList());

        return Board.builder()
                .title(this.title)
                .content(this.content)
                .isPinned(this.isPinned)
                .category(category)
                .user(user)
                .delYn(this.delYn != null ? this.delYn : DelYN.N)
                .tags(tagList)
                .build();
    }
}
