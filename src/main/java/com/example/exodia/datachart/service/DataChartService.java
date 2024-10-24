package com.example.exodia.datachart.service;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.datachart.dto.DepartmentRankingDto;
import com.example.exodia.datachart.dto.PositionRankingDto;
import com.example.exodia.datachart.dto.SalaryDistributionDto;
import com.example.exodia.user.domain.Status;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.department.repository.DepartmentRepository;
import com.example.exodia.position.repository.PositionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataChartService {

    private final UserRepository userRepository;

    public DataChartService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, Long> getGenderStatistics() {
        return userRepository.findAllByDelYn(DelYN.N).stream()
                .collect(Collectors.groupingBy(user -> user.getGender().toString(), Collectors.counting()));
    }

    public Map<String, Long> getHireTypeStatistics() {
        return userRepository.findAllByDelYn(DelYN.N).stream()
                .collect(Collectors.groupingBy(user -> user.getHireType().toString(), Collectors.counting()));
    }

    public Map<String, List<Long>> getYearlyHireAndResignation(int year) {
        List<Long> hireCounts = new ArrayList<>(Collections.nCopies(12, 0L));
        List<Long> resignationCounts = new ArrayList<>(Collections.nCopies(12, 0L));

        List<User> users = userRepository.findAllByDelYn(DelYN.N);
        for (User user : users) {
            if (user.getCreatedAt().getYear() == year) {
                int month = user.getCreatedAt().getMonthValue() - 1;
                hireCounts.set(month, hireCounts.get(month) + 1);
            }
            if (user.getStatus() == Status.퇴사 && user.getUpdatedAt().getYear() == year) {
                int month = user.getUpdatedAt().getMonthValue() - 1;
                resignationCounts.set(month, resignationCounts.get(month) + 1);
            }
        }

        Map<String, List<Long>> result = new HashMap<>();
        result.put("입사", hireCounts);
        result.put("퇴사", resignationCounts);
        return result;
    }

    public List<DepartmentRankingDto> getDepartmentRanking() {
        return userRepository.findAllByDelYn(DelYN.N).stream()
                .collect(Collectors.groupingBy(user -> user.getDepartment().getName(), Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new DepartmentRankingDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(DepartmentRankingDto::getCount).reversed())
                .collect(Collectors.toList());
    }

    public List<PositionRankingDto> getPositionRanking() {
        return userRepository.findAllByDelYn(DelYN.N).stream()
                .collect(Collectors.groupingBy(user -> user.getPosition().getName(), Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new PositionRankingDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(PositionRankingDto::getCount).reversed())
                .collect(Collectors.toList());
    }

    public List<SalaryDistributionDto> getSalaryDistribution() {
        return userRepository.findAllByDelYn(DelYN.N).stream()
                .collect(Collectors.groupingBy(user -> calculateSalaryRange(user.getPosition().getBaseSalary()), Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new SalaryDistributionDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(SalaryDistributionDto::getRange))
                .collect(Collectors.toList());
    }

    public Map<String, Long> getSeniorityStatistics() {
        return userRepository.findAllByDelYn(DelYN.N).stream()
                .collect(Collectors.groupingBy(user -> calculateSeniority(user.getCreatedAt()), Collectors.counting()));
    }

    private String calculateSalaryRange(double baseSalary) {
        if (baseSalary < 3000) return "3000미만";
        else if (baseSalary < 4000) return "3000-4000";
        else if (baseSalary < 5000) return "4000-5000";
        else return "5000이상";
    }

    private String calculateSeniority(LocalDateTime createdAt) {
        int years = LocalDateTime.now().getYear() - createdAt.getYear();
        return years >= 10 ? "10년 이상" : years + "년";
    }
}