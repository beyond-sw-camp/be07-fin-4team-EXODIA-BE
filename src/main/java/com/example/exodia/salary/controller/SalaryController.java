// SalaryController.java

package com.example.exodia.salary.controller;

import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.salary.domain.Salary;
import com.example.exodia.salary.dto.SalaryDto;
import com.example.exodia.salary.dto.SalaryUpdateDto;
import com.example.exodia.salary.service.SalaryService;
import com.example.exodia.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/salary")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    // 사원의 월급 명세서 조회
    @GetMapping("/my")
    public ResponseEntity<SalaryDto> getMySalary(User user) {
        Salary salary = salaryService.getSalarySlip(user);
        int yearsOfService = salaryService.calculateYearsOfService(user);  // 입사년차 계산
        return ResponseEntity.ok(SalaryDto.fromEntity(salary, yearsOfService));
    }

    // 상세 페이지를 위한 사원별 월급 명세서 조회
    @GetMapping("/detail/{userNum}")
    public ResponseEntity<SalaryDto> getSalaryDetail(@PathVariable String userNum) {
        Optional<Salary> salary = salaryService.getSalaryByUserNum(userNum);
        if (salary.isPresent()) {
            User user = salary.get().getUser();
            int yearsOfService = salaryService.calculateYearsOfService(user);  // 입사년차 계산
            return ResponseEntity.ok(SalaryDto.fromEntity(salary.get(), yearsOfService));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSalaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Salary> salaryPage = salaryService.getAllSalaries(PageRequest.of(page, size));

        List<SalaryDto> salaryDtos = salaryPage.getContent().stream()
                .map(salary -> SalaryDto.fromEntity(salary, salaryService.calculateYearsOfService(salary.getUser())))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("salaries", salaryDtos);
        response.put("currentPage", salaryPage.getNumber());
        response.put("totalItems", salaryPage.getTotalElements());
        response.put("totalPages", salaryPage.getTotalPages());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/byPosition/{positionId}")
    public ResponseEntity<Map<String, Object>> getSalariesByPosition(
            @PathVariable Long positionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Salary> salaryPage = salaryService.getSalariesByPosition(positionId, PageRequest.of(page, size));

        List<SalaryDto> salaryDtos = salaryPage.getContent().stream()
                .map(salary -> SalaryDto.fromEntity(salary, salaryService.calculateYearsOfService(salary.getUser())))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("salaries", salaryDtos);
        response.put("currentPage", salaryPage.getNumber());
        response.put("totalItems", salaryPage.getTotalElements());
        response.put("totalPages", salaryPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{userNum}")
    public ResponseEntity<?> updateSalary(@RequestBody SalaryUpdateDto salaryUpdateDto) {
        try {
            salaryService.updateSalary(salaryUpdateDto);
            return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "Salary updated successfully", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update salary: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updateByPercentage/{userNum}")
    public ResponseEntity<?> updateSalaryByPercentage(@PathVariable String userNum, @RequestParam double percentage) {
        salaryService.updateSalaryByPercentage(userNum, percentage);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "Salary updated by percentage successfully", null), HttpStatus.OK);
    }
}