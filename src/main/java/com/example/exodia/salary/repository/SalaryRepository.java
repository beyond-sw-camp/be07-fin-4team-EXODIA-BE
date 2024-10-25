package com.example.exodia.salary.repository;

import com.example.exodia.salary.domain.Salary;
import com.example.exodia.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalaryRepository extends JpaRepository<Salary, Long> {
    Optional<Salary> findByUser(User user);
    Optional<Salary> findByUser_UserNum(String userNum);
    List<Salary> findByUser_Position_Id(Long positionId);
    Page<Salary> findByUser_Position_Id(Long positionId, Pageable pageable);
    Optional<Salary> findByUserUserNum(String userNum);
}


