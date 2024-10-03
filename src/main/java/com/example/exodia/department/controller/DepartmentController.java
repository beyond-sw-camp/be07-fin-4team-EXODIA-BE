package com.example.exodia.department.controller;

import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.department.domain.Department;
import com.example.exodia.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestParam String name, @RequestParam(required = false) Long parentId) {
        try {
            Department department = departmentService.createDepartment(name, parentId);
            return new ResponseEntity<>(new CommonResDto(HttpStatus.CREATED, "Department created successfully", department), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @RequestParam String name, @RequestParam(required = false) Long parentId) {
        try {
            Department department = departmentService.updateDepartment(id, name, parentId);
            return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "Department updated successfully", department), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/hierarchy/{id}")
    public ResponseEntity<?> updateDepartmentHierarchy(@PathVariable Long id, @RequestParam Long newParentId) {
        try {
            departmentService.updateDepartmentHierarchy(id, newParentId);
            return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "Department hierarchy updated successfully", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllDepartments() {
        try {
            List<Department> departments = departmentService.getAllDepartments();
            return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "Departments retrieved successfully", departments), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveAllDepartments(@RequestBody List<Department> departments) {
        try {
            departmentService.saveAllDepartments(departments);
            return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "Departments saved successfully", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}

