package com.example.exodia.evalution.domain;

import com.example.exodia.attendance.domain.DayStatus;
import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.department.domain.Department;
import com.example.exodia.position.domain.Position;
import com.example.exodia.subevalution.domain.SubEvalution;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Evalution extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Score score; // 평가 점수

    private double average; // 총 평점

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 이걸 통해서 동 팀의 팀장, 상위 팀의 소장 등의 평가를 받을 수 있음.

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department; // 팀명

    @ManyToOne
    @JoinColumn(name = " position_id", nullable = false)
    private Position position;

    @ManyToOne
    @JoinColumn(name = "sub_evalution_id", nullable = false)
    private SubEvalution subEvalution;
}