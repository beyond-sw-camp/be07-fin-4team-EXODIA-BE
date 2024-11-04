package com.example.exodia.evalution.repository;

import com.example.exodia.evalution.domain.Evalution;
import com.example.exodia.evalutionFrame.subevalution.domain.SubEvalution;
import com.example.exodia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvalutionRepository extends JpaRepository<Evalution, Long> {
    List<Evalution> findBySubEvalution(SubEvalution subEvalution);
    // 중복 검증 (평가자userNum + 피평가자userNum + subEvalution 이 3가지 같은 조건을 충족하면 됨)
    boolean existsByEvaluatorAndTargetAndSubEvalution(User evaluator, User target, SubEvalution subEvalution);
    Evalution findByTargetAndSubEvalution(User target, SubEvalution subEvalution);

}
