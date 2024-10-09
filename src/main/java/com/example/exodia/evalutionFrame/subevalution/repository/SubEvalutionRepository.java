package com.example.exodia.evalutionFrame.subevalution.repository;

import com.example.exodia.evalutionFrame.evalutionMiddle.domain.Evalutionm;
import com.example.exodia.evalutionFrame.subevalution.domain.SubEvalution;
import com.example.exodia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubEvalutionRepository extends JpaRepository<SubEvalution, Long> {
    List<SubEvalution> findByUser(User user);
    List<SubEvalution> findByEvalutionm(Evalutionm evalutionm);
    Optional<SubEvalution> findByUserIdAndEvalutionmId(Long userId, Long evalutionmId);
}
