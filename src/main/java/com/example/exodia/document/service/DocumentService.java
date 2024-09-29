package com.example.exodia.document.service;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.data.DefaultRepositoryTagsProvider;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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
import com.example.exodia.document.domain.EsDocument;
import com.example.exodia.document.dto.DocDetailResDto;
import com.example.exodia.document.dto.DocHistoryResDto;
import com.example.exodia.document.dto.DocListResDto;
import com.example.exodia.document.dto.DocReqDto;
import com.example.exodia.document.dto.DocUpdateReqDto;
import com.example.exodia.document.repository.DocumentCRepository;
import com.example.exodia.document.repository.DocumentPRepository;
import com.example.exodia.document.repository.DocumentTypeRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
@Transactional
public class DocumentService {

	private final DocumentCRepository documentCRepository;
	private final DocumentPRepository documentPRepository;
	private final DocumentTypeRepository documentTypeRepository;
	private final UserRepository userRepository;
	private final RedisService redisService;
	private final UploadAwsFileService uploadAwsFileService;
	private final DocumentSearchService documentSearchService;
	private final DefaultRepositoryTagsProvider repositoryTagsProvider;
	private S3Client s3Client;

	@Autowired
	public DocumentService(DocumentCRepository documentCRepository, DocumentPRepository documentPRepository,
		DocumentTypeRepository documentTypeRepository, UserRepository userRepository, RedisService redisService,
		UploadAwsFileService uploadAwsFileService, DocumentSearchService documentSearchService, S3Client s3Client,
		DefaultRepositoryTagsProvider repositoryTagsProvider) {
		this.documentCRepository = documentCRepository;
		this.documentPRepository = documentPRepository;
		this.documentTypeRepository = documentTypeRepository;
		this.userRepository = userRepository;
		this.redisService = redisService;
		this.uploadAwsFileService = uploadAwsFileService;
		this.documentSearchService = documentSearchService;
		this.s3Client = s3Client;
		this.repositoryTagsProvider = repositoryTagsProvider;
	}

	public DocumentC saveDoc(List<MultipartFile> files, DocReqDto docReqDto) throws IOException {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		// s3에 업로드
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

		DocumentP documentP = DocumentP.toEntity(documentC.getId(), documentType, "1");
		documentPRepository.save(documentP);
		// documentPRepository.flush();
		//
		// documentC.updateDocumentP(documentP);
		// documentCRepository.save(documentC);

		// opens search 인덱싱
		EsDocument esDocument = EsDocument.toEsDocument(documentC);
		documentSearchService.indexDocuments(esDocument);
		return documentC;
	}

	//	첨부파일 다운로드
	public ResponseEntity<Resource> downloadFile(Long id) throws IOException {
		DocumentC doc = documentCRepository.findById(id).orElseThrow(() -> new NoSuchFileException("파일이 존재하지 않습니다"));

		// S3에서 파일 다운로드
		String filePath = doc.getFilePath();
		filePath = filePath.substring(filePath.indexOf(".com/") + 5);
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket("exodia-file")
			.key("document/" + filePath)
			.build();

		ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
		InputStreamResource resource = new InputStreamResource(s3Object);

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

		// opens search 인덱싱
		EsDocument esDocument = EsDocument.toEsDocument(newDoc);
		documentSearchService.indexDocuments(esDocument);

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
		while (documentC != null) {
			DocumentP documentP = documentC.getDocumentP();
			if (documentP != null) {
				documentC = documentCRepository.findById(documentP.getId())
					.orElseThrow(() -> new EntityNotFoundException("히스토리가 존재하지 않습니다."));
				if (documentC.getDocumentP() != null) {
					versions.add(documentC.fromHistoryEntity());
				} else {
					versions.add(documentC.fromHistoryEntity("1"));
				}
			}else{
				return versions;
			}
		}
		return versions;
	}

	// 문서 삭제 -> 해당 문서만 삭제
	public void deleteDocument(Long id) {
		DocumentC documentC = documentCRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("문서가 존재하지 않습니다."));
		documentC.softDelete();
		documentSearchService.deleteDocument(id.toString());
		documentCRepository.save(documentC);
	}

	// 모든 타입 조회
	public List<String> getAllTypeNames() {
		List<DocumentType> documentTypes = documentTypeRepository.findAll();
		return documentTypes.stream()
			.map(DocumentType::getTypeName)
			.collect(Collectors.toList());
	}
}
