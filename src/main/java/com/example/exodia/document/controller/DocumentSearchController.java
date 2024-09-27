package com.example.exodia.document.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.document.domain.EsDocument;
import com.example.exodia.document.service.DocumentSearchService;

@RestController
@RequestMapping("/es/document")
public class DocumentSearchController {

	private final DocumentSearchService documentSearchService;

	@Autowired
	public DocumentSearchController(DocumentSearchService documentSearchService) {
		this.documentSearchService = documentSearchService;
	}

	// 인덱싱
	@PostMapping("/index")
	public ResponseEntity<?> indexDocuments(@RequestBody EsDocument esDocument) {
			documentSearchService.indexDocuments(esDocument);
			return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "es: 문서 인덱싱 성공", null));
	}

	// 검색
	@GetMapping("/search")
	public ResponseEntity<?> searchDocuments(@RequestParam String keyword) {
		List<EsDocument> documents = documentSearchService.searchDocuments(keyword);
		return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "es: 문서 검색 성공", documents));
	}

	// 삭제
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteDocument(@PathVariable String id) {
		documentSearchService.deleteDocument(id);
		return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "es: 문서 삭제 완료", null));
	}

}
