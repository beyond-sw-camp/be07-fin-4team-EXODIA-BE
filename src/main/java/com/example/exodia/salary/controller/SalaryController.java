package com.example.exodia.salary.controller;

import com.example.exodia.salary.domain.Salary;
import com.example.exodia.salary.dto.SalaryDto;
import com.example.exodia.salary.service.SalaryService;
import com.example.exodia.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/salary")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    // 사원의 월급 명세서 조회
    @GetMapping("/my")
    public ResponseEntity<Salary> getMySalary(User user) {
        Salary salary = salaryService.getSalarySlip(user);
        return ResponseEntity.ok(salary);
    }

    // 관리자용 급여 관리 저장
    @PostMapping("/manage")
    public ResponseEntity<Salary> saveSalary(@RequestBody Salary salary) {
        Salary savedSalary = salaryService.saveSalary(salary);
        return ResponseEntity.ok(savedSalary);
    }

    @DeleteMapping("/manage/{salaryId}")
    public ResponseEntity<Void> deleteSalary(@PathVariable Long salaryId) {
        salaryService.deleteSalary(salaryId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<SalaryDto>> getAllSalaries() {
        List<Salary> salaries = salaryService.getAllSalaries();
        List<SalaryDto> salaryDtos = salaries.stream()
                .map(salary -> SalaryDto.fromEntity(salary))
                .collect(Collectors.toList());
        return ResponseEntity.ok(salaryDtos);
    }

    @GetMapping("/byPosition/{positionId}")
    public ResponseEntity<List<SalaryDto>> getSalariesByPosition(@PathVariable Long positionId) {
        List<Salary> salaries = salaryService.getSalariesByPosition(positionId);
        List<SalaryDto> salaryDtos = salaries.stream()
                .map(SalaryDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(salaryDtos);
    }


}
