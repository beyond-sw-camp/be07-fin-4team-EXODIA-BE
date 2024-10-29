package com.example.exodia.datachart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PositionRankingDto {
    private String position;
    private Long count;
}