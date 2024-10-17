package com.example.exodia.salary.repository;

import com.example.exodia.salary.domain.PositionSalary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PositionSalaryRepository extends JpaRepository<PositionSalary, Long> {
    Optional<PositionSalary> findByPositionId(Long positionId);
}
