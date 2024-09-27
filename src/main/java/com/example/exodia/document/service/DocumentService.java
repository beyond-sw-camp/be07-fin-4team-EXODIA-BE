package com.example.exodia.document.service;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.common.service.RedisService;
import com.example.exodia.common.service.UploadAwsFileService;
import com.example.exodia.document.domain.DocumentC;
import com.example.exodia.document.domain.DocumentP;
import com.example.exodia.document.domain.DocumentType;
import com.example.exodia.document.dto.DocDetailResDto;
import com.example.exodia.document.dto.DocHistoryResDto;
import com.example.exodia.document.dto.DocListResDto;
import com.example.exodia.document.dto.DocReqDto;
import com.example.exodia.document.dto.DocRevertReqDto;
import com.example.exodia.document.dto.DocUpdateReqDto;
import com.example.exodia.document.repository.DocumentCRepository;
import com.example.exodia.document.repository.DocumentPRepository;
import com.example.exodia.document.repository.DocumentTypeRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;

@Service
@Transactional
public class DocumentService {

	private final DocumentCRepository documentCRepository;
	private final DocumentPRepository documentPRepository;
	private final DocumentTypeRepository documentTypeRepository;
	private final UserRepository userRepository;
	private final RedisService redisService;
	private final UploadAwsFileService uploadAwsFileService;

	@Autowired
	public DocumentService(DocumentCRepository documentCRepository, DocumentPRepository documentPRepository,
		DocumentTypeRepository documentTypeRepository, UserRepository userRepository, RedisService redisService,
		UploadAwsFileService uploadAwsFileService) {
		this.documentCRepository = documentCRepository;
		this.documentPRepository = documentPRepository;
		this.documentTypeRepository = documentTypeRepository;
		this.userRepository = userRepository;
		this.redisService = redisService;
		this.uploadAwsFileService = uploadAwsFileService;
	}

	public DocumentC saveDoc(List<MultipartFile> files, DocReqDto docReqDto) throws IOException {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		String fileName = files.get(0).getOriginalFilename();
		List<String> fileDownloadUrl = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files, "document");

		DocumentType documentType = documentTypeRepository.findByTypeName(docReqDto.getTypeName())
			.orElseGet(() -> {
				return documentTypeRepository.save(
					DocumentType.builder()
						.typeName(docReqDto.getTypeName()).delYn(DelYN.N).build());
			});

		DocumentC documentC = docReqDto.toEntity(docReqDto, user, fileName, fileDownloadUrl.get(0), documentType);
		documentCRepository.save(documentC);
		return documentC;
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
			throw new IOException("파일을 읽어올 수 없음: " + doc.getFileName());
		}
	}

	// 	전체 문서 조회
	public List<DocListResDto> getDocList() {
		// 생성시간 == 수정시간 doc만 조회 -> 수정되지 않은 모든 데이터
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		List<DocumentC> docs = documentCRepository.findAll();
		List<DocListResDto> docListResDtos = new ArrayList<>();
		for (DocumentC doc : docs) {
			docListResDtos.add(doc.fromEntityList());
		}
		return docListResDtos;
	}

	// 최근 열람 문서 조회
	public List<DocListResDto> getDocListByViewdAt() {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		// User user = userRepository.findByUserNum(userNum)
		// 	.orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		List<Object> docIds = redisService.getViewdListValue(userNum);
		List<DocumentC> docs = new ArrayList<>();
		List<DocListResDto> docListResDtos = new ArrayList<>();

		for (Object docId : docIds) {
			DocumentC doc = documentCRepository.findById(((Integer)docId).longValue())
				.orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
			docs.add(doc);
		}
		for (DocumentC doc : docs) {
			docListResDtos.add(doc.fromEntityList());
		}
		return docListResDtos;
	}

	// 최근 수정 문서 조회
	public List<DocListResDto> getDocListByUpdatedAt() {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

		List<Object> docIds = redisService.getUpdatedListValue(userNum);
		List<DocumentC> docs = new ArrayList<>();
		List<DocListResDto> docListResDtos = new ArrayList<>();

		for (Object docId : docIds) {
			DocumentC doc = documentCRepository.findById(((Integer)docId).longValue())
				.orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
			docs.add(doc);
		}
		for (DocumentC doc : docs) {
			docListResDtos.add(doc.fromEntityList());
		}
		return docListResDtos;
	}

	// 	문서 상세조회
	public DocDetailResDto getDocDetail(Long id) {
		DocumentC documentC = documentCRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("문서가 존재하지 않습니다."));

		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

		List<Object> docIds = redisService.getViewdListValue(userNum);
		if (docIds.contains(id.intValue())) {
			redisService.removeViewdListValue(userNum, id);
		}
		redisService.setViewdListValue(userNum, documentC.getId());
		return documentC.fromEntity();
	}

	// 문서 업데이트
	public DocumentC updateDoc(List<MultipartFile> files, DocUpdateReqDto docUpdateReqDto) throws IOException {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		// 현재 문서
		DocumentC documentC = documentCRepository.findById(docUpdateReqDto.getId())
			.orElseThrow(() -> new EntityNotFoundException("문서가 존재하지 않습니다."));

		List<Object> docIds = redisService.getUpdatedListValue(userNum);
		if (docIds.contains(docUpdateReqDto.getId().intValue())) {
			redisService.removeUpdatedListValue(userNum, docUpdateReqDto.getId());
		}
		redisService.setUpdatedListValue(userNum, documentC.getId());

		String version = "";
		if (documentC.getDocumentP() == null) {
			version = "2";
		} else {
			version = String.valueOf(Integer.parseInt(documentC.getDocumentP().getVersion()) + 1);
		}

		DocumentP newDocP = documentPRepository.save(
			DocumentP.toEntity(docUpdateReqDto.getId(), documentC.getDocumentType(), version));

		// 새로운 파일로 수정
		String fileName = files.get(0).getOriginalFilename();
		List<String> fileDownloadUrl = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files, "document");
		// String s3FilePath = uploadAwsFileService.uploadSingleFileAndReturnPath(file, "document");

		DocumentType documentType = documentTypeRepository.findByTypeName(docUpdateReqDto.getTypeName())
			.orElseThrow(() -> new EntityNotFoundException("폴더가 존재하지 않습니다."));

		DocumentC newDoc = docUpdateReqDto.updatetoEntity(docUpdateReqDto, newDocP, user, fileName,
			fileDownloadUrl.get(0), documentType);
		documentCRepository.save(newDoc);
		return newDoc;
	}

	// 문서 버전
	//  public DocDetailResDto revertToVersion(DocRevertReqDto docRevertReqDto) {
	// 	DocumentC currentDoc = documentCRepository.findById(docRevertReqDto.getId())
	// 		.orElseThrow(() -> new EntityNotFoundException("문서가 존재하지 않습니다."));
	//
	// 	currentDoc.updateContents(docRevertReqDto.version);
	// 	currentDoc.updateUpdatedAt();
	// 	documentCRepository.save(currentDoc);
	//
	// 	return currentDoc.fromEntity();
	// }

	// 	문서 히스토리 조회
	public List<DocHistoryResDto> getDocumentVersions(Long id) {
		DocumentC documentC = documentCRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("문서가 존재하지 않습니다."));

		List<DocHistoryResDto> versions = new ArrayList<>();
		versions.add(documentC.fromHistoryEntity());

		// 모든 버전(부모 문서들) 조회
		if (documentC != null && documentC.getDocumentP() != null) {
			DocumentP documentP = documentC.getDocumentP();
			documentC = documentCRepository.findById(documentP.getId())
				.orElseThrow(() -> new EntityNotFoundException("히스토리가 존재하지 않습니다."));
			versions.add(documentC.fromHistoryEntity());
		}
		return versions;
	}

	// 문서 삭제 -> 해당 문서만 삭제
	public void deleteDocument(Long id) {
		DocumentC documentC = documentCRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("문서가 존재하지 않습니다."));
		documentC.softDelete();
		documentCRepository.save(documentC);
	}

	// 	문서 업데이트
	// public DocumentC updateDoc(MultipartFile file, DocUpdateReqDto docUpdateReqDto) throws IOException {
	// 	String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
	// 	User user = userRepository.findByUserNum(userNum)
	// 		.orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));
	//
	// 	// 현재 문서
	// 	DocumentC documentC = documentCRepository.findById(docUpdateReqDto.getId())
	// 		.orElseThrow(() -> new EntityNotFoundException("문서가 존재하지 않습니다."));
	//
	// 	List<Object> docIds = redisService.getUpdatedListValue(userNum);
	// 	if (docIds.contains(docUpdateReqDto.getId().intValue())) {
	// 		redisService.removeUpdatedListValue(userNum, docUpdateReqDto.getId());
	// 	}
	// 	redisService.setUpdatedListValue(userNum, documentC.getId());
	//
	// 	String version = "";
	// 	if (documentC.getDocumentP() == null) {
	// 		version = "1";
	// 	} else {
	// 		version = String.valueOf(Integer.parseInt(documentC.getDocumentP().getVersion()) + 1);
	// 	}
	//
	// 	DocumentP newDocP = documentPRepository.save(DocumentP.toEntity(docUpdateReqDto.getId(), documentC.getDocumentType(), version));
	//
	//
	// 	// 새로운 파일로 수정
	// 	String s3FilePath = uploadAwsFileService.uploadSingleFileAndReturnPath(file, "document");
	// 	String fileName = file.getOriginalFilename();
	// 	String fileDownloadUrl = uploadAwsFileService.generatePresignedUrl(fileName);
	//
	// 	DocumentType documentType = documentTypeRepository.findByTypeName(docUpdateReqDto.getTypeName())
	// 		.orElseThrow(() -> new EntityNotFoundException("폴더가 존재하지 않습니다."));
	//
	// 	DocumentC newDoc = DocumentC.updatetoEntity(docUpdateReqDto, newDocP, user, fileName, s3FilePath, fileDownloadUrl,
	// 		documentType);
	// 	// 자식 문서 추가
	//
	//
	// 	// DocumentC savedC = documentCRepository.save(
	// 	// 	DocumentC.updatetoEntity(docUpdateReqDto, user, fileName, s3FilePath, fileDownloadUrl, documentType));
	// 	// // 부모 문서 추가
	// 	// DocumentP updateP = documentP.updateEntity(docUpdateReqDto.getId(), documentP.getVersion());
	// 	// 자식 문서 업데이트
	// 	// savedC.updateDocumentP(updateP);
	//
	// 	// documentCRepository.save(savedC);
	// 	// documentPRepository.save(updateP);
	// 	return documentCRepository.save(newDoc);
	// }
}
