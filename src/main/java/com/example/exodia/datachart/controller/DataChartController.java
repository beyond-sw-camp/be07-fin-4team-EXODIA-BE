package com.example.exodia.datachart.controller;

import com.example.exodia.datachart.service.DataChartService;
import com.example.exodia.datachart.dto.DepartmentRankingDto;
import com.example.exodia.datachart.dto.PositionRankingDto;
import com.example.exodia.datachart.dto.SalaryDistributionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stats")
public class DataChartController {

    private final DataChartService dataChartService;

    public DataChartController(DataChartService dataChartService) {
        this.dataChartService = dataChartService;
    }

    @GetMapping("/gender")
    public ResponseEntity<Map<String, Long>> getGenderStatistics() {
        Map<String, Long> genderStats = dataChartService.getGenderStatistics();
        return ResponseEntity.ok(genderStats);
    }

    @GetMapping("/hireType")
    public ResponseEntity<Map<String, Long>> getHireTypeStatistics() {
        Map<String, Long> hireTypeStats = dataChartService.getHireTypeStatistics();
        return ResponseEntity.ok(hireTypeStats);
    }

    @GetMapping("/yearlyHireAndResignation/{year}")
    public ResponseEntity<Map<String, List<Long>>> getYearlyHireAndResignation(@PathVariable int year) {
        Map<String, List<Long>> yearlyStats = dataChartService.getYearlyHireAndResignation(year);
        return ResponseEntity.ok(yearlyStats);
    }

    @GetMapping("/departmentRanking")
    public ResponseEntity<List<DepartmentRankingDto>> getDepartmentRanking() {
        List<DepartmentRankingDto> ranking = dataChartService.getDepartmentRanking();
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/positionRanking")
    public ResponseEntity<List<PositionRankingDto>> getPositionRanking() {
        List<PositionRankingDto> ranking = dataChartService.getPositionRanking();
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/salaryDistribution")
    public ResponseEntity<List<SalaryDistributionDto>> getSalaryDistribution() {
        List<SalaryDistributionDto> salaryDistribution = dataChartService.getSalaryDistribution();
        return ResponseEntity.ok(salaryDistribution);
    }

    @GetMapping("/seniority")
    public ResponseEntity<Map<String, Long>> getSeniorityStatistics() {
        Map<String, Long> seniorityStats = dataChartService.getSeniorityStatistics();
        return ResponseEntity.ok(seniorityStats);
    }
}
