package com.example.exodia.salary.controller;

import com.example.exodia.salary.domain.Salary;
import com.example.exodia.salary.service.SalaryService;
import com.example.exodia.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/salary")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    @GetMapping("/my")
    public ResponseEntity<Salary> getMySalary(User user) {
        Salary salary = salaryService.getSalarySlip(user);
        return ResponseEntity.ok(salary);
    }

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
}
