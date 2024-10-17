//package com.example.exodia.salary.controller;
//
//import com.example.exodia.salary.domain.PositionSalary;
//import com.example.exodia.salary.domain.Salary;
//import com.example.exodia.salary.service.PositionSalaryService;
//import com.example.exodia.salary.service.SalaryService;
//import com.example.exodia.salary.repository.PositionSalaryRepository;
//
//import com.example.exodia.user.domain.User;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.*;
//
//@RestController
//@RequestMapping("/position-salary")
//@RequiredArgsConstructor
//public class PositionSalaryController {
//
//    private final PositionSalaryService positionSalaryService;
//    private final PositionSalaryRepository positionSalaryRepository;
//
//    @GetMapping
//    public ResponseEntity<List<PositionSalary>> getPositionSalaries() {
//        List<PositionSalary> positionSalaries = positionSalaryService.getAllPositionSalaries();
//        return ResponseEntity.ok(positionSalaries);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<PositionSalary> updatePositionSalary(@PathVariable Long id, @RequestBody PositionSalary salary) {
//        PositionSalary existingSalary = positionSalaryRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("연봉 정보를 찾을 수 없습니다."));
//        existingSalary.setBaseSalary(salary.getBaseSalary());
//        PositionSalary updatedSalary = positionSalaryRepository.save(existingSalary);
//        return ResponseEntity.ok(updatedSalary);
//    }
//    @PostMapping("/update")
//    public ResponseEntity<PositionSalary> updatePositionSalary(@RequestBody PositionSalary positionSalary) {
//        PositionSalary updatedSalary = positionSalaryService.updatePositionSalary(positionSalary);
//        return ResponseEntity.ok(updatedSalary);
//    }
//}
