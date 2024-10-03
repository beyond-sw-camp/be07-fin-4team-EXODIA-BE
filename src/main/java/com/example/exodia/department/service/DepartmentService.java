package com.example.exodia.department.service;

import com.example.exodia.department.domain.Department;
import com.example.exodia.department.repository.DepartmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    public List<Department> getDepartmentHierarchy() {
        return departmentRepository.findByParentDepartmentIsNull();
    }

    @Transactional
    public Department createDepartment(String name, Long parentId) {
        Department parent = parentId != null ? departmentRepository.findById(parentId).orElse(null) : null;
        Department department = new Department(name, parent);
        return departmentRepository.save(department);
    }

    @Transactional
    public Department updateDepartment(Long id, String name, Long parentId) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));
        Department parent = parentId != null ? departmentRepository.findById(parentId).orElse(null) : null;
        department.update(name, parent);
        return departmentRepository.save(department);
    }

    @Transactional
    public void saveAllDepartments(List<Department> departments) {
        departments.forEach(departmentRepository::save);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }
}
