package com.example.exodia.position.controller;

import com.example.exodia.position.domain.Position;
import com.example.exodia.position.repository.PositionRepository;
import com.example.exodia.salary.domain.PositionSalary;
import com.example.exodia.salary.repository.PositionSalaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionRepository positionRepository;
    private final PositionSalaryRepository positionSalaryRepository;

    // 직급 목록 조회
    @GetMapping
    public ResponseEntity<List<Position>> getPositions() {
        List<Position> positions = positionRepository.findAll();
        return ResponseEntity.ok(positions);
    }

    // 직급 추가
    @PostMapping
    public ResponseEntity<Position> createPosition(@RequestBody Position position) {
        Position savedPosition = positionRepository.save(position);
        return ResponseEntity.ok(savedPosition);
    }

    // 특정 직급에 연차별 연봉 추가
    @PostMapping("/{positionId}/salaries")
    public ResponseEntity<PositionSalary> addPositionSalary(@PathVariable Long positionId, @RequestBody PositionSalary salary) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("직급을 찾을 수 없습니다."));
        salary.setPosition(position);
        PositionSalary savedSalary = positionSalaryRepository.save(salary);
        return ResponseEntity.ok(savedSalary);
    }
}

