package com.example.exodia.position.controller;

import com.example.exodia.position.domain.Position;
import com.example.exodia.position.dto.PositionDto;
import com.example.exodia.position.repository.PositionRepository;
import com.example.exodia.position.service.PositionService;
import com.example.exodia.salary.domain.Salary;
import com.example.exodia.salary.dto.SalaryDto;
import com.example.exodia.salary.service.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionRepository positionRepository;
    private final PositionService positionService;
    private final SalaryService salaryService;

    @PostMapping
    public ResponseEntity<Position> createPosition(@RequestBody Position position) {
        Position savedPosition = positionRepository.save(position);
        return ResponseEntity.ok(savedPosition);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 직급을 찾을 수 없습니다."));

        positionRepository.delete(position);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<PositionDto>> getPositions() {
        List<PositionDto> positions = positionService.getAllPositions()
                .stream()
                .map(position -> new PositionDto(position.getId(), position.getName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(positions);
    }

    @GetMapping("/{positionId}/salaries")
    public ResponseEntity<List<SalaryDto>> getSalariesByPosition(@PathVariable Long positionId) {
        List<Salary> salaries = salaryService.getSalariesByPosition(positionId);
        List<SalaryDto> salaryDtos = salaries.stream()
                .map(salary -> SalaryDto.fromEntity(salary, salaryService.calculateYearsOfService(salary.getUser())))
                .collect(Collectors.toList());

        return ResponseEntity.ok(salaryDtos);
    }
}
