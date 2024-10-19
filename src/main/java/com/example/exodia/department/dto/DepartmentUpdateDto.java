package com.example.exodia.department.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DepartmentUpdateDto {
    private Long departmentId;
    private String description;

}
