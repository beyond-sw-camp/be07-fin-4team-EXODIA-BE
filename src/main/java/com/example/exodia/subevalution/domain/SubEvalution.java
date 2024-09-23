package com.example.exodia.subevalution.domain;

import com.example.exodia.evalution.domain.Evalution;
import com.example.exodia.evalutionm.domain.Evalutionm;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class SubEvalution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content; // 작성한 분류 명

    @OneToOne
    @JoinColumn(name = "evalutionm_id", nullable = false)
    private Evalutionm evalutionm; // 중분류 상속

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "subEvalution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evalution> evalutions = new ArrayList<>();
}
