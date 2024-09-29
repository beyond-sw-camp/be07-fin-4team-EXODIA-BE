package com.example.exodia.evalutionFrame.evalutionMiddle.domain;


import com.example.exodia.evalutionFrame.evalutionBig.domain.Evalutionb;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/*중분류*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Evalutionm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String mName; // 중분류 명

    @ManyToOne
    @JoinColumn(name = "evalutionb_id", nullable = false)
    private Evalutionb evalutionb; // 대분류 상속

//    @OneToOne(mappedBy = "evalutionm", cascade = CascadeType.ALL, orphanRemoval = true)
//    private SubEvalution subEvalution;

}
