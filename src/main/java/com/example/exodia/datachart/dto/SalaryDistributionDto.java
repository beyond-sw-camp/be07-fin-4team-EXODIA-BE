package com.example.exodia.datachart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SalaryDistributionDto {
    private String range;
    private Long count;
}