package com.example.exodia.position.domain;


import com.example.exodia.salary.domain.PositionSalary;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "dep_position")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL)
    private List<PositionSalary> salaries = new ArrayList<>();

    public Position(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}

