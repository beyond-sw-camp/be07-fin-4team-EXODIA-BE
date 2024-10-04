package com.example.exodia.salary.repository;

import com.example.exodia.salary.domain.PositionSalary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionSalaryRepository extends JpaRepository<PositionSalary, Long> {
}
