package com.example.exodia.document.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.exodia.document.domain.Document;
import com.example.exodia.document.domain.DocumentVersion;

@Repository
public interface DocumentRepository extends JpaRepository<Document,Long> {
	// 현재문서의 모든 버전 조회
	List<Document> findAllByDocumentVersion(DocumentVersion documentVersion);

	// 최신 버전의 문서들 조회
	Page<Document> findAllByStatusAndDepartmentId(String status, Long departmentId, Pageable pageable);

	// 문서 롤백을 위한 이전 버전들 조회
	List<Document> findByDocumentVersionAndIdGreaterThan(DocumentVersion documentVersion, Long id);


	// 검색
	@Query("SELECT d FROM Document d WHERE d.fileName LIKE %:keyword% OR d.description LIKE %:keyword%")
	List<Document> searchByKeyword(@Param("keyword") String keyword);
}
