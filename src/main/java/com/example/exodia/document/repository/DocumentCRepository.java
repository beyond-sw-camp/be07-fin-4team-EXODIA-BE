package com.example.exodia.document.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.exodia.document.domain.DocumentC;
import com.example.exodia.document.dto.DocListResDto;
import com.example.exodia.user.domain.User;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface DocumentCRepository extends JpaRepository<DocumentC,Long> {
	DocumentC findByDocumentP_Id(Long documentPId);

}
