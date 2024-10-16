package com.example.exodia.board.dto;

import com.example.exodia.board.domain.Board;
import com.example.exodia.board.domain.Category;
import com.example.exodia.board.domain.Tags;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.department.domain.Department;
import com.example.exodia.user.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardSaveReqDto {

    private String title;
    private String content;
    private Category category;
    private String userNum;
    private boolean isPinned;
    private Long hits = 0L;
    private Department department;
    private List<Long> tagIds;

    @Builder.Default
    @JsonIgnore
    private List<MultipartFile> files = Collections.emptyList();

    @Builder.Default
    private DelYN delYn = DelYN.N;

    public Board toEntity(User user) {
        return Board.builder()
                .title(this.title)
                .content(this.content)
                .category(this.category)
                .delYn(this.delYn != null ? this.delYn : DelYN.N)
                .isPinned(this.isPinned)
                .user(user)
                .hits(this.hits)
                .build();
    }

}
