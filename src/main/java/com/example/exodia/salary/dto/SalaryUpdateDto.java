package com.example.exodia.salary.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SalaryUpdateDto {

    private String userNum;
    private double baseSalary;

    private Double percentageAdjustment;
    private Double newFinalSalary;

}
