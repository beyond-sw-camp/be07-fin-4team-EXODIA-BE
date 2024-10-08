package com.example.exodia.submit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.exodia.submit.domain.Submit;
import com.example.exodia.user.domain.User;

@Repository
public interface SubmitRepository extends JpaRepository<Submit, Long> {
	List<Submit> findAllByUser(User user);
}
