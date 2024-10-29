package com.example.exodia.evalutionavg.domain;

import com.example.exodia.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/*
* 사실상 이전 데이터 evalutionb, evalutionm은 자동으로 입력되고
* sub-evalution, evalution 은 휘발성 데이터를 지향 하므로
* 이 evalutionAvg 에서 최종 평점 처리를 진행 + 인사평가가 열리는 기간 설정을 위한 날짜 받기
* */

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvalutionAvg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator; // 평가자

    @ManyToOne
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser; // 평가 대상자

    @Column(nullable = false)
    private int totalScore; // 총점

    @Column(nullable = false)
    private int totalCount; // 평가 개수

    public double getAverageScore() {
        return totalCount > 0 ? (double) totalScore / totalCount : 0;
    }
}
