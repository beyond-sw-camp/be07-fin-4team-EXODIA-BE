package com.example.exodia.department.repository;

import com.example.exodia.department.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findDepartmentById(Long departmentId);
    List<Department> findByParentDepartment_Id(Long parentId);
    Optional<Department> findByName(String name);
    List<Department> findByParentDepartmentIsNull();
}

