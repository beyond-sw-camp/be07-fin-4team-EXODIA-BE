package com.example.exodia.document.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.exodia.document.domain.DocumentC;
import com.example.exodia.document.domain.DocumentP;
import com.example.exodia.document.dto.DocDetailResDto;
import com.example.exodia.document.dto.DocListResDto;
import com.example.exodia.document.dto.DocReqDto;
import com.example.exodia.document.repository.DocumentCRepository;
import com.example.exodia.document.repository.DocumentPRepository;

@Service
@Transactional
public class DocumentService {

	private final DocumentCRepository documentCRepository;
	private final DocumentPRepository documentPRepository;

	@Autowired
	public DocumentService(DocumentCRepository documentCRepository, DocumentPRepository documentPRepository) {
		this.documentCRepository = documentCRepository;
		this.documentPRepository = documentPRepository;
	}

	// 	문서 업로드
	private String uploadDir = "/Users/suhyun/Documents/pot_doc";

	// 파일 로컬에 저장
	public Path saveFile(MultipartFile file, DocReqDto docReqDto) throws IOException {
		String fileName = file.getOriginalFilename();
		Path filePath = Paths.get(uploadDir + "/" + fileName);
		Files.write(filePath, file.getBytes());

		String path = filePath.toString();
		String fileContent = new String(file.getBytes(), StandardCharsets.UTF_8);

		Long id = documentCRepository.save(DocumentC.toEntity(docReqDto, path, fileContent)).getId();
		documentPRepository.save(DocumentP.toEntity(docReqDto, id));
		return filePath;
	}

	//	첨부파일 다운로드
	public ResponseEntity<Resource> downloadFile(Long id, HttpServletResponse response) throws IOException {
		DocumentC doc = documentCRepository.findById(id).orElseThrow(() -> new NoSuchFileException("파일이 존재하지 않습니다"));
		Resource resource = new UrlResource(Paths.get(doc.getFilePath()).toUri());

		if (resource.exists() && resource.isReadable()) {
			return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
				.body(resource);
		} else {
			throw new IOException("File not found or not readable: " + doc.getFileName());
		}
	}

	// 	전체 문서 조회
	public List<DocListResDto> getDocList() {
		// 생성시간 != 수정시간이 같은 doc만 조회
		List<DocumentC> docs = documentCRepository.findDocsWhereCreatedAtEqualUpdatedAt();
		List<DocListResDto> docListResDtos = new ArrayList<>();
		for (DocumentC doc : docs) {
			docListResDtos.add(doc.fromEntityList());
		}
		return docListResDtos;
	}

	// 최근 열람 문서 조회
	public List<DocListResDto> getDocListByViewdAt() {
		List<DocumentC> docs = documentCRepository.findTopByOrderByViewedAtDesc();
		List<DocListResDto> docListResDtos = new ArrayList<>();
		for (DocumentC doc : docs) {
			docListResDtos.add(doc.fromEntityList());
		}
		return docListResDtos;
	}

	// 	문서 상세조회
	public DocDetailResDto getDocDetail(Long id) {
		DocumentC documentC = documentCRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("문서가 존재하지 않습니다."));
		documentC.updateViewdAt();    // 조회 시간 업데이트
		return documentC.fromEntity();
	}

	// 	문서 업데이트
	public DocDetailResDto updateDocDetail(Long id) {
		DocumentC documentC = documentCRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("문서가 존재하지 않습니다."));
		documentC.updateUpdatedAt();    // 수정 시간 업데이트
		return documentC.fromEntity();
	}

	// 	문서 히스토리 조회

	// 	최근 열람 문서 조회
	// 	최근 업데이트 문서 조회

	public void updateViewdAt() {

	}
}
