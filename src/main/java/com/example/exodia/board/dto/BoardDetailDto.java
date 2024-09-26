package com.example.exodia.board.dto;


import com.example.exodia.board.domain.Category;
import com.example.exodia.comment.dto.CommentResDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDetailDto {

    private Long id;
    private String title;
    private String content;
    private Category category;
    private Long hits;
    private String user_num;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> filePaths;
    private List<CommentResDto> comments;
}
