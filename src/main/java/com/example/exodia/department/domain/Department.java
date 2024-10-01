package com.example.exodia.department.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parentDepartment;

    public Department(String name, Department parentDepartment) {
        this.name = name;
        this.parentDepartment = parentDepartment;
    }

    public void update(String name, Department parentDepartment) {
        this.name = name;
        this.parentDepartment = parentDepartment;
    }
}