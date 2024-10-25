package com.example.exodia.datachart.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DepartmentRankingDto {
    private String department;
    private Long count;
}
