package com.example.exodia.submit.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.exodia.submit.domain.Submit;
import com.example.exodia.submit.domain.SubmitStatus;
import com.example.exodia.submit.domain.SubmitType;
import com.example.exodia.user.domain.User;

@Repository
public interface SubmitRepository extends JpaRepository<Submit, Long> {
	Page<Submit> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);

	List<Submit> findAllBySubmitStatusAndSubmitType(SubmitStatus submitStatus, String submitType);
}
