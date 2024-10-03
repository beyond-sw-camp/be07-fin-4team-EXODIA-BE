package com.example.exodia.department.service;

import com.example.exodia.department.domain.Department;
import com.example.exodia.department.repository.DepartmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    public List<Map<String, Object>> getDepartmentHierarchy() {
        List<Department> allDepartments = departmentRepository.findAll();
        List<Map<String, Object>> hierarchy = new ArrayList<>();

        Map<Long, Map<String, Object>> departmentMap = new HashMap<>();

        for (Department department : allDepartments) {
            Map<String, Object> departmentData = new HashMap<>();
            departmentData.put("id", department.getId());
            departmentData.put("name", department.getName());
            departmentData.put("parentId", department.getParentDepartment() != null ? department.getParentDepartment().getId() : null);
            departmentData.put("children", new ArrayList<Map<String, Object>>());
            departmentMap.put(department.getId(), departmentData);
        }

        for (Department department : allDepartments) {
            if (department.getParentDepartment() != null) {
                Long parentId = department.getParentDepartment().getId();
                List<Map<String, Object>> children = (List<Map<String, Object>>) departmentMap.get(parentId).get("children");
                children.add(departmentMap.get(department.getId()));
            } else {
                hierarchy.add(departmentMap.get(department.getId()));
            }
        }

        return hierarchy;
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
        for (Department department : departments) {
            Department existingDepartment = departmentRepository.findById(department.getId())
                    .orElse(null);

            if (existingDepartment != null) {
                Department parent = department.getParentDepartment() != null ?
                        departmentRepository.findById(department.getParentDepartment().getId()).orElse(null) : null;
                existingDepartment.setName(department.getName());
                existingDepartment.setParentDepartment(parent);
                departmentRepository.save(existingDepartment);
            } else {
                Department parent = department.getParentDepartment() != null ?
                        departmentRepository.findById(department.getParentDepartment().getId()).orElse(null) : null;
                department.setParentDepartment(parent);
                departmentRepository.save(department);
            }
        }
    }


    @Transactional
    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }
}
