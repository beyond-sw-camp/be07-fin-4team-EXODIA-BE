package com.example.exodia.evalution.repository;

import com.example.exodia.evalution.domain.Evalution;
import com.example.exodia.evalutionFrame.subevalution.domain.SubEvalution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvalutionRepository extends JpaRepository<Evalution, Long> {
    List<Evalution> findBySubEvalution(SubEvalution subEvalution);
}
