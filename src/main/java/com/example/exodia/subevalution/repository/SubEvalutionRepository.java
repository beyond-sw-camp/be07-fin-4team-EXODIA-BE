package com.example.exodia.subevalution.repository;

import com.example.exodia.subevalution.domain.SubEvalution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubEvalutionRepository extends JpaRepository<SubEvalution, Long> {
}
