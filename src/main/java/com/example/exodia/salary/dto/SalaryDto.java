package com.example.exodia.salary.dto;

import com.example.exodia.salary.domain.Salary;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDto {
    private String userNum;
    private String userName;
    private String departmentName;
    private String positionName;
    private double baseSalary;
    private int yearsOfService;

    public static SalaryDto fromEntity(Salary salary) {
        User user = salary.getUser();
        return new SalaryDto(
                user.getUserNum(),
                user.getName(),
                user.getDepartment().getName(),
                user.getPosition().getName(),
                salary.getBaseSalary(),
                Period.between(user.getCreatedAt().toLocalDate(), LocalDate.now()).getYears()
        );
    }
}
