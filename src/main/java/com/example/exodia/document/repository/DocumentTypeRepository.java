package com.example.exodia.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.exodia.document.domain.DocumentType;

@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {
}
