package com.example.exodia.salary.repository;

import com.example.exodia.salary.domain.Salary;
import com.example.exodia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalaryRepository extends JpaRepository<Salary, Long> {
    Optional<Salary> findByUser(User user);
}
