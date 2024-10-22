package com.example.exodia.document.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.exodia.department.domain.Department;
import com.example.exodia.document.domain.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
	List<Tag> findAllByDepartment(Department department);
	boolean existsByTagName(String tagName);
}
