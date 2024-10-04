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

    @GetMapping
    public ResponseEntity<List<Position>> getPositions() {
        List<Position> positions = positionRepository.findAll();
        return ResponseEntity.ok(positions);
    }

    @PostMapping
    public ResponseEntity<Position> createPosition(@RequestBody Position position) {
        Position savedPosition = positionRepository.save(position);
        return ResponseEntity.ok(savedPosition);
    }

    @PostMapping("/{positionId}/salaries")
    public ResponseEntity<PositionSalary> addPositionSalary(@PathVariable Long positionId, @RequestBody PositionSalary salary) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("직급을 찾을 수 없습니다."));
        salary.setPosition(position);
        PositionSalary savedSalary = positionSalaryRepository.save(salary);
        return ResponseEntity.ok(savedSalary);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 직급을 찾을 수 없습니다."));

        positionSalaryRepository.deleteByPosition(position);

        positionRepository.delete(position);
        return ResponseEntity.ok().build();
    }
}


