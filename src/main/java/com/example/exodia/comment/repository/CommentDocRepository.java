package com.example.exodia.comment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.exodia.comment.domain.CommentDoc;
import com.example.exodia.document.domain.Document;

@Repository
public interface CommentDocRepository extends JpaRepository<CommentDoc, Long> {
	List<CommentDoc> findByDocumentOrderByCreatedAtDesc(Document document);

}
