package com.example.exodia.evalutionFrame.evalutionBig.domain;


import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


/*대분류*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Evalutionb {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bName; // 대분류 명


//    @OneToMany(mappedBy = "evalutionb", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Evalutionm> evalutionms = new ArrayList<>();

}
