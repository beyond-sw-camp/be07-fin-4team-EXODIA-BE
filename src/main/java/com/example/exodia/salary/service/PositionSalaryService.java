package com.example.exodia.salary.service;

import com.example.exodia.position.repository.PositionRepository;
import com.example.exodia.salary.domain.PositionSalary;
import com.example.exodia.salary.repository.PositionSalaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionSalaryService {

    private final PositionSalaryRepository positionSalaryRepository;
    private final PositionRepository positionRepository;

    public List<PositionSalary> getAllPositionSalaries() {
        return positionSalaryRepository.findAll();
    }

    public PositionSalary updatePositionSalary(PositionSalary positionSalary) {
        PositionSalary existingSalary = positionSalaryRepository.findById(positionSalary.getId())
                .orElseThrow(() -> new IllegalArgumentException("연봉 정보를 찾을 수 없습니다."));

        existingSalary.setBaseSalary(positionSalary.getBaseSalary());
        return positionSalaryRepository.save(existingSalary);
    }
}
