package com.example.exodia.department.controller;

import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.department.domain.Department;
import com.example.exodia.department.dto.DepartmentDto;
import com.example.exodia.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/hierarchy")
    public ResponseEntity<List<Map<String, Object>>> getDepartmentHierarchy() {
        List<Map<String, Object>> hierarchy = departmentService.getDepartmentHierarchy();
        return new ResponseEntity<>(hierarchy, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        Long parentId = request.get("parentId") != null ? Long.valueOf(request.get("parentId").toString()) : null;

        Department department = departmentService.createDepartment(name, parentId);
        return new ResponseEntity<>(department, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        String name = (String) request.get("name");
        Long parentId = request.get("parentId") != null ? Long.valueOf(request.get("parentId").toString()) : null;

        try {
            Department updatedDepartment = departmentService.updateDepartment(id, name, parentId);
            return ResponseEntity.ok(updatedDepartment);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/saveAll")
    public ResponseEntity<Void> saveAllDepartments(@RequestBody List<Department> departments) {
        departmentService.saveAllDepartments(departments);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping
    public ResponseEntity<List<DepartmentDto>> getDepartments() {
        List<DepartmentDto> departments = departmentService.getAllDepartments()
                .stream()
                .map(department -> new DepartmentDto(department.getId(), department.getName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(departments);
    }

    @GetMapping("/name/{id}")
    public ResponseEntity<?> getDepartmentName(@PathVariable Long id) {
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "부서 이름 조회 성공", departmentService.getDepartmentName(id)));
    }
}
