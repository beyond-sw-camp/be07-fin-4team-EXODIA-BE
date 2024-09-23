package com.example.exodia.evalutionb.domain;


import com.example.exodia.evalutionm.domain.Evalutionm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/*대분류*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Evalutionb {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bName; // 대분류 명

    @OneToMany(mappedBy = "evalutionb", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evalutionm> evalutionms = new ArrayList<>();
}
