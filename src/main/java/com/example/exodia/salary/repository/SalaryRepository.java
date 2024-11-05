package com.example.exodia.salary.repository;

import com.example.exodia.salary.domain.Salary;
import com.example.exodia.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SalaryRepository extends JpaRepository<Salary, Long> {
    @EntityGraph(attributePaths = {"user", "user.department", "user.position"})
    Optional<Salary> findByUser(User user);
    @EntityGraph(attributePaths = {"user", "user.department", "user.position"})
    Optional<Salary> findByUser_UserNum(String userNum);
    List<Salary> findByUser_Position_Id(Long positionId);
//    @Query("")
    @EntityGraph(attributePaths = {"user", "user.department", "user.position"})
    Page<Salary> findByUser_Position_Id(Long positionId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "user.department", "user.position"})
    Page<Salary> findAll(Pageable pageable);

    Optional<Salary> findByUserUserNum(String userNum);
}


