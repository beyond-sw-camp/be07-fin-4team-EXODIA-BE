package com.example.exodia.evalution.repository;

import com.example.exodia.evalution.domain.Evalution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvalutionRepository extends JpaRepository<Evalution, Long> {

}
