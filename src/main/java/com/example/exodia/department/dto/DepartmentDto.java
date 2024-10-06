package com.example.exodia.department.dto;

import com.example.exodia.department.domain.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class DepartmentDto {
    private Long id;
    private String name;

    public DepartmentDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static DepartmentDto fromEntity(Department department) {
        return new DepartmentDto(department.getId(), department.getName());
    }

}

