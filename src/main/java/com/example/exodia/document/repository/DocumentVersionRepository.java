package com.example.exodia.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.exodia.document.domain.DocumentVersion;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
}
