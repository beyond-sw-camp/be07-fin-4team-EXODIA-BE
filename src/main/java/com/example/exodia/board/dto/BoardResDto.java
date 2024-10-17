package com.example.exodia.board.dto;

import com.example.exodia.board.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResDto {
    private Long id;
    private String title;
    private String content;
    private Category category;
    private boolean isPinned;
    private Long hits;


}
