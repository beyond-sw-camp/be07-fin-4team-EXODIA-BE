package com.example.exodia.document.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.exodia.document.domain.Document;
import com.example.exodia.document.domain.DocumentType;
import com.example.exodia.document.domain.DocumentVersion;
import com.example.exodia.document.dto.DocHistoryResDto;

@Repository
public interface DocumentRepository extends JpaRepository<Document,Long> {
	// Document findByDocumentP_Id(Long documentPId);
	List<Document> findAllByDocumentVersion(DocumentVersion documentVersion);
	List<Document> findAllByDocumentType(DocumentType documentType);
	List<Document> findAllByStatus(String status);
	List<Document> findByDocumentVersionAndIdGreaterThan(DocumentVersion documentVersion, Long id);
	List<Document> findAllByIdAndStatus(Long id, String status);
}
