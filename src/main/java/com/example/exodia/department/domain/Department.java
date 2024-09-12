package com.example.exodia.department.domain;

import org.hibernate.annotations.*;
import org.springframework.data.annotation.Id;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Entity
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
