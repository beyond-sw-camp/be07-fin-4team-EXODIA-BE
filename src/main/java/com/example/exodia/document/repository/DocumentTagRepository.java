package com.example.exodia.document.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.exodia.document.domain.DocumentTag;

@Repository
public interface DocumentTagRepository extends JpaRepository<DocumentTag, Long> {
	DocumentTag findByTagName(String tagName);
}
