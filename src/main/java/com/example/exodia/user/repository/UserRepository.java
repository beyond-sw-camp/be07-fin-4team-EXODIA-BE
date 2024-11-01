package com.example.exodia.user.repository;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.department.domain.Department;
import com.example.exodia.position.domain.Position;
import com.example.exodia.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserNum(String userNum);
    Optional<User> findByUserNumAndDelYn(String userNum, DelYN delYn);
    List<User> findAllByDelYn(DelYN delYn);
    Page<User> findAllByDelYn(DelYN delYn, Pageable pageable);

    Optional<User> findByNameAndPosition(String userName, Position position);
    List<User> findAllByDepartmentName(String departmentName); // notification 에서 인사팀의 가지고오기
    List<User> findAllByDepartmentId(Long departmentId);
    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId and u.delYn = :delYn ORDER BY u.position.id ASC")
    List<User> findAllByDepartmentIdAndDelYn(@Param("departmentId")Long departmentId, @Param("delYn")DelYN delYn);
    @Query("SELECT u FROM User u WHERE u.delYn = :delYn ORDER BY u.position.id ASC")
    Page<User> findByDelYn(@Param("delYn")DelYN delYN, Pageable pageable);
    @Query("SELECT u FROM User u WHERE u.name LIKE CONCAT('%', :name, '%') AND u.delYn = :delYn ORDER BY u.position.id ASC")
    Page<User> findByNameContainingAndDelYn(@Param("name")String name, @Param("delYn")DelYN delYN, Pageable pageable);
    @Query("SELECT u FROM User u JOIN u.department d WHERE d.name LIKE CONCAT('%', :departmentName, '%') AND u.delYn = :delYn ORDER BY u.position.id ASC")
    Page<User> findByDepartmentNameContainingAndDelYn(@Param("departmentName")String departmentName, @Param("delYn")DelYN delYN, Pageable pageable);
    @Query("SELECT u FROM User u JOIN u.position p WHERE p.name LIKE CONCAT('%', :positionName, '%') AND u.delYn = :delYn ORDER BY u.position.id ASC")
    Page<User> findByPositionNameContainingAndDelYn(@Param("positionName")String positionName, @Param("delYn")DelYN delYN, Pageable pageable);
    Page<User> findByNameContainingOrDepartmentNameContainingOrPositionNameContainingAndDelYn(
            String name, String departmentName, String positionName, DelYN delYN, Pageable pageable
    );
    @Query("SELECT u FROM User u " +
    "LEFT JOIN u.department d " +
    "LEFT JOIN u.position p " +
    "WHERE u.delYn = :delYn1 AND u.name LIKE CONCAT('%', :name, '%') " +
    "OR d IS NOT NULL AND d.delYn = :delYn2 AND d.name LIKE CONCAT('%', :departmentName, '%') " +
    "OR p IS NOT NULL AND p.delYn = :delYn3 AND p.name LIKE CONCAT('%', :positionName, '%') " +
    "ORDER BY u.position.id ASC")
    Page<User> findByDelYnAndNameContainingOrDelYnAndDepartmentNameContainingOrDelYnAndPositionNameContaining(
            @Param("delYn1")DelYN delYn1, @Param("name")String name, @Param("delYn2")DelYN delYn2, @Param("departmentName")String departmentName, @Param("delYn3")DelYN delYn3, @Param("positionName")String positionName, Pageable pageable
    );

    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId ORDER BY u.position.id ASC")
    List<User> findByDepartmentId(@Param("departmentId")Long departmentId);
    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId AND u.name LIKE CONCAT('%', :name, '%') ORDER BY u.position.id ASC")
    List<User> findByDepartmentIdAndNameContaining(Long departmentId, String name);

    long countByDepartmentId(Long departmentId);

    @Query("SELECT u.userNum FROM User u WHERE u.userNum LIKE CONCAT(:date, '%') ORDER BY u.userNum DESC LIMIT 1")
    String findLastUserNum(@Param("date") String date);

    @Query("SELECT m.user FROM Manager m WHERE m.user.department.id = :departmentId")
    List<User> findManagersByDepartmentId(@Param("departmentId") Long departmentId);

}
