package com.example.exodia.evalutionavg.repository;

import com.example.exodia.evalutionavg.domain.EvalutionAvg;
import com.example.exodia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvalutionAvgRepository extends JpaRepository<EvalutionAvg, Long> {
    Optional<EvalutionAvg> findByEvaluatorAndTargetUser(User evaluator, User targetUser);
}
