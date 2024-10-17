package com.example.exodia.board.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardPinReqDto {
    private Long boardId;
    private Boolean isPinned;
}
