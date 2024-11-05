package com.example.exodia.submit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.exodia.submit.domain.SubmitLine;


@Repository
public interface SubmitLineRepository extends JpaRepository<SubmitLine, Long> {
	// Optional<SubmitLine> findByUserNumAndSubmitId(String userNum, Long submitId);
	List<SubmitLine> findBySubmitIdOrderByUserNumDesc(Long submitId);
	Page<SubmitLine> findAllByUserNumOrderByCreatedAtDesc(String userNum, Pageable pageable);

	@Query("SELECT sl FROM SubmitLine sl JOIN User u ON sl.userNum = u.userNum WHERE sl.submit.id = :submitId ORDER BY u.position.id DESC")
	List<SubmitLine> findBySubmitIdOrderByUserPositionId(@Param("submitId") Long submitId);

	SubmitLine findBySubmitIdAndUserNum(Long submitId, String userNum);

}
