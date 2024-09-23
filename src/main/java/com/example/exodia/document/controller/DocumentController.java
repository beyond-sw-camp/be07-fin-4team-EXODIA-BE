package com.example.exodia.document.controller;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.exodia.document.domain.DocumentC;
import com.example.exodia.document.dto.DocDetailResDto;
import com.example.exodia.document.dto.DocListResDto;
import com.example.exodia.document.dto.DocReqDto;
import com.example.exodia.document.dto.DocRevertReqDto;
import com.example.exodia.document.dto.DocUpdateReqDto;
import com.example.exodia.document.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/document")
public class DocumentController {

	private final DocumentService documentService;

	@Autowired
	public DocumentController(DocumentService documentService) {
		this.documentService = documentService;
	}

	// 	문서 업로드
	@PostMapping("/uploadFile")
	public ResponseEntity<?> uploadFile(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart(value = "data") DocReqDto docReqDto) {
		try {
			documentService.saveDoc(file, docReqDto);
			return ResponseEntity.ok("파일 저장 성공");
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("파일 저장 실패: " + e.getMessage());
		}
	}

	//	첨부파일 다운로드
	@GetMapping("/downloadFile/{id}")
	public ResponseEntity<?> downloadFile(@PathVariable Long id, HttpServletResponse response) throws IOException {
		return documentService.downloadFile(id, response);
	}

	// 	전체 문서 조회
	@GetMapping("/list/all")
	public ResponseEntity<?> docList() {
		return ResponseEntity.ok(documentService.getDocList());
	}

	// 	최근 열람 문서 조회
	@GetMapping("/list/viewd")
	public ResponseEntity<List<?>> docListByViewdAt() {
		return ResponseEntity.ok(documentService.getDocListByViewdAt());
	}

	// 	최근 업데이트 문서 조회
	@GetMapping("/list/updated")
	public ResponseEntity<List<?>> docListByUpdatedAt() {
		return ResponseEntity.ok(documentService.getDocListByUpdatedAt());
	}

	// 	문서 상세조회
	@GetMapping("/detail/{id}")
	public ResponseEntity<?> detail(@PathVariable Long id) {
		return ResponseEntity.ok(documentService.getDocDetail(id));
	}

	// 	문서 업데이트
	@PostMapping("/update/{id}")
	public ResponseEntity<?> update(@RequestPart(value = "file", required = false) MultipartFile file,
		@RequestPart(value = "data") DocUpdateReqDto docUpdateReqDto) {
		try {
			documentService.updateDoc(file, docUpdateReqDto);
			return ResponseEntity.ok("파일 저장 성공");
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("파일 저장 실패: " + e.getMessage());
		}
	}


	//  문서 버전 되돌리기
	// @PostMapping("/revert")
	// public ResponseEntity<DocDetailResDto> revertToVersion(DocRevertReqDto docRevertReqDto) {
	// 	DocDetailResDto response = documentService.revertToVersion(docRevertReqDto);
	// 	return ResponseEntity.ok(response);
	// }


	// 	문서 히스토리 조회
	@GetMapping("/{documentId}/versions")
	public ResponseEntity<List<DocumentC>> getDocumentVersions(@PathVariable Long documentId){
		List<DocumentC> documentVersions = documentService.getDocumentVersions(documentId);
		return ResponseEntity.ok(documentVersions);
	}


}
