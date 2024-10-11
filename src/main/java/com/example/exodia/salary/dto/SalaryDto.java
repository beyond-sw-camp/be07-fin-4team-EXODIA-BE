package com.example.exodia.salary.dto;

import com.example.exodia.salary.domain.Salary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDto {

    private String userNum;
    private String userName;
    private String departmentName;
    private String positionName;
    private double baseSalary;
    private double nationalPension;
    private double healthInsurance;
    private double longTermCare;
    private double employmentInsurance;
    private double totalTax;
    private double finalSalary;
    private int yearsOfService;   

    public static SalaryDto fromEntity(Salary salary, int yearsOfService) {
        return new SalaryDto(
                salary.getUser().getUserNum(),
                salary.getUser().getName(),
                salary.getUser().getDepartment().getName(),
                salary.getUser().getPosition().getName(),
                salary.getBaseSalary(),
                salary.getTaxAmount().getNationalPension(),
                salary.getTaxAmount().getHealthInsurance(),
                salary.getTaxAmount().getLongTermCare(),
                salary.getTaxAmount().getEmploymentInsurance(),
                salary.getTaxAmount().getTotalTax(),
                salary.getFinalSalary(),
                yearsOfService
        );
    }
}
