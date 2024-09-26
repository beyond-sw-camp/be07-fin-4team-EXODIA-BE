package com.example.exodia.board.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardPinReqDto {
    private Long userId;
    private Boolean isPinned;
}
