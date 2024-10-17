package com.example.exodia.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.exodia.document.domain.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
	Tag findByTagName(String name);
}
