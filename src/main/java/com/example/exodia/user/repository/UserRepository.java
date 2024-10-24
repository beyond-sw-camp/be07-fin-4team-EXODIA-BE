package com.example.exodia.user.repository;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.department.domain.Department;
import com.example.exodia.position.domain.Position;
import com.example.exodia.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserNum(String userNum);
    Optional<User> findByUserNumAndDelYn(String userNum, DelYN delYn);
    List<User> findAllByDelYn(DelYN delYn);
    Page<User> findAllByDelYn(DelYN delYn, Pageable pageable);

    Optional<User> findByNameAndPosition(String userName, Position position);
    List<User> findAllByDepartmentName(String departmentName); // notification 에서 인사팀의 가지고오기
    List<User> findAllByDepartmentId(Long departmentId);
    List<User> findAllByDepartmentIdAndDelYn(Long departmentId, DelYN delYn);
    Page<User> findByDelYn(DelYN delYN, Pageable pageable);
    Page<User> findByNameContainingAndDelYn(String name, DelYN delYN, Pageable pageable);
    Page<User> findByDepartmentNameContainingAndDelYn(String departmentName, DelYN delYN, Pageable pageable);
    Page<User> findByPositionNameContainingAndDelYn(String positionName, DelYN delYN, Pageable pageable);
    Page<User> findByNameContainingOrDepartmentNameContainingOrPositionNameContainingAndDelYn(
            String name, String departmentName, String positionName, DelYN delYN, Pageable pageable
    );
    List<User> findByDepartmentId(Long departmentId);
    List<User> findByDepartmentIdAndNameContaining(Long departmentId, String name);

    long countByDepartmentId(Long departmentId);
}
