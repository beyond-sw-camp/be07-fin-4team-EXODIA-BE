package com.example.exodia.document.service;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.exodia.common.service.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.common.service.RedisService;
import com.example.exodia.common.service.UploadAwsFileService;
import com.example.exodia.document.domain.Document;
import com.example.exodia.document.domain.DocumentTag;
import com.example.exodia.document.domain.DocumentVersion;
import com.example.exodia.document.domain.EsDocument;
import com.example.exodia.document.domain.Tag;
import com.example.exodia.document.dto.DocDetailResDto;
import com.example.exodia.document.dto.DocHistoryResDto;
import com.example.exodia.document.dto.DocListResDto;
import com.example.exodia.document.dto.DocSaveReqDto;
import com.example.exodia.document.dto.DocTagListReqDto;
import com.example.exodia.document.dto.DocTagReqDto;
import com.example.exodia.document.dto.DocUpdateReqDto;
import com.example.exodia.document.repository.DocumentRepository;
import com.example.exodia.document.repository.DocumentTagRepository;
import com.example.exodia.document.repository.DocumentVersionRepository;
import com.example.exodia.document.repository.TagRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
@Transactional
public class DocumentService {

	private final DocumentRepository documentRepository;
	private final DocumentVersionRepository documentVersionRepository;
	private final UserRepository userRepository;
	private final RedisService redisService;
	private final UploadAwsFileService uploadAwsFileService;
	private final DocumentSearchService documentSearchService;
	private final DocumentTagRepository documentTagRepository;
	private final TagRepository tagRepository;
	private S3Client s3Client;
	private KafkaProducer kafkaProducer;

	@Autowired
	public DocumentService(DocumentRepository documentRepository, DocumentVersionRepository documentVersionRepository,
						   UserRepository userRepository, RedisService redisService,
						   UploadAwsFileService uploadAwsFileService, DocumentSearchService documentSearchService, S3Client s3Client, KafkaProducer kafkaProducer,
						   DocumentTagRepository documentTagRepository, TagRepository tagRepository) {
		this.documentRepository = documentRepository;
		this.documentVersionRepository = documentVersionRepository;
		this.userRepository = userRepository;
		this.redisService = redisService;
		this.uploadAwsFileService = uploadAwsFileService;
		this.documentSearchService = documentSearchService;
		this.s3Client = s3Client;
		this.kafkaProducer = kafkaProducer;
		this.documentTagRepository = documentTagRepository;
		this.tagRepository = tagRepository;
	}

	@Transactional
	public Document saveDoc(List<MultipartFile> files, DocSaveReqDto docSaveReqDto) throws IOException{
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		// s3에 업로드
		String fileName = files.get(0).getOriginalFilename();
		List<String> fileDownloadUrl = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files, "document");

		// doc 저장
		Document document = docSaveReqDto.toEntity(docSaveReqDto, user, fileName, fileDownloadUrl.get(0));
		documentRepository.save(document);

		// 태그는 이미 존재하고 있으니 -> DocTag에 추가
		for (String tagName : docSaveReqDto.getTags()) {
			DocumentTag documentTag = DocumentTag.builder().document(document).tagName(tagName).delYn(DelYN.N).build();
			documentTagRepository.save(documentTag);
			document.getTags().add(documentTag);
		}

		// docVersion 생성
		DocumentVersion documentVersion = DocumentVersion.toEntity(document);
		documentVersionRepository.save(documentVersion);

		// doc 수정
		document.updateDocumentVersion(documentVersion);
		documentRepository.save(document);

		// opens search 저장
		// EsDocument esDocument = EsDocument.toEsDocument(document);
		// documentSearchService.indexDocuments(esDocument);
		return document;
	}

	//	첨부파일 다운로드
	public ResponseEntity<Resource> downloadFile(Long id) throws IOException {
		Document doc = documentRepository.findById(id).orElseThrow(() -> new NoSuchFileException("파일이 존재하지 않습니다"));

		// S3에서 파일 다운로드
		String filePath = doc.getFilePath();

		filePath = filePath.substring(filePath.indexOf(".com/") + 5);
		String fileKey = filePath.substring(filePath.indexOf("document/"));
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket("exodia-file")
				.key(fileKey)
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
	public Page<DocListResDto> getDocList(Pageable pageable) {
		// 생성시간 == 수정시간 doc만 조회 -> 수정되지 않은 모든 데이터
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

		Page<Document> docs = documentRepository.findAllByStatus("now", pageable);
		return docs.map(Document::fromEntityList);
	}

	// 최근 열람 문서 조회
	public Page<DocListResDto> getDocListByViewedAt(Pageable pageable) {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		List<Object> docIds = redisService.getViewdListValue(userNum);

		// docIds 리스트에서 문서를 조회하고, 필터링한 결과를 리스트로 수집
		List<Document> documents = docIds.stream()
				.map(docId -> documentRepository.findById(((Integer) docId).longValue())
						.orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다.")))
				.filter(document -> "now".equals(document.getStatus()))
				.collect(Collectors.toList());

		// 리스트를 페이지네이션하여 Page 형태로 변환
		List<DocListResDto> docListResDtos = documents.stream()
				.map(Document::fromEntityList)
				.collect(Collectors.toList());

		// Pageable 객체를 활용하여 Page로 변환
		int start = Math.min((int) pageable.getOffset(), docListResDtos.size());
		int end = Math.min((start + pageable.getPageSize()), docListResDtos.size());
		return new PageImpl<>(docListResDtos.subList(start, end), pageable, docListResDtos.size());
	}

	// 최근 수정 문서 조회
	public Page<DocListResDto> getDocListByUpdatedAt(Pageable pageable) {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		List<Object> docIds = redisService.getUpdatedListValue(userNum);

		// docIds 리스트에서 문서를 조회하고, 필터링한 결과를 리스트로 수집
		List<Document> documents = docIds.stream()
				.map(docId -> documentRepository.findById(((Integer) docId).longValue())
						.orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다.")))
				.filter(document -> "now".equals(document.getStatus()))
				.collect(Collectors.toList());

		// 리스트를 페이지네이션하여 Page 형태로 변환
		List<DocListResDto> docListResDtos = documents.stream()
				.map(Document::fromEntityList)
				.collect(Collectors.toList());

		// Pageable 객체를 활용하여 Page로 변환
		int start = Math.min((int) pageable.getOffset(), docListResDtos.size());
		int end = Math.min((start + pageable.getPageSize()), docListResDtos.size());
		return new PageImpl<>(docListResDtos.subList(start, end), pageable, docListResDtos.size());
	}

	// 	문서 상세조회
	public DocDetailResDto getDocDetail(Long id) {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

		Document document = documentRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("문서가 존재하지 않습니다."));

		List<String> docTagName = new ArrayList<>();
		for (DocumentTag documentTag : document.getTags()) {
			docTagName.add(documentTag.getTagName());
		}

		// 최근 조회 목록: redis에 저장
		List<Object> docIds = redisService.getViewdListValue(userNum);

		// 이미 있으면 순서 변경
		if (docIds.contains(id.intValue())) {
			redisService.removeViewdListValue(userNum, id);
		}
		redisService.setViewdListValue(userNum, document.getId());

		return document.fromEntity(docTagName);
	}

	// 문서 업데이트
	public Document updateDoc(List<MultipartFile> files, DocUpdateReqDto docUpdateReqDto) throws IOException {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		// 현재 문서 상태 변경
		Document document = documentRepository.findById(docUpdateReqDto.getId())
				.orElseThrow(() -> new EntityNotFoundException("문서가 존재하지 않습니다."));
		document.updateStatus();
		documentRepository.save(document);
		DocumentVersion documentVersion = document.getDocumentVersion();

		// s3에 업로드
		String fileName = files.get(0).getOriginalFilename();
		List<String> fileDownloadUrl = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files, "document");

		// 새로운 문서 저장
		Document newDocument = docUpdateReqDto.toEntity(docUpdateReqDto, user, document, fileName, fileDownloadUrl.get(0));
		documentRepository.save(newDocument);

		// 태그 추가
		List<String> docTagNames = docUpdateReqDto.getTags();
		for (String docTag :docTagNames) {
			DocumentTag documentTag = DocumentTag.builder().document(newDocument).tagName(docTag).delYn(DelYN.N).build();
			newDocument.getTags().add(documentTag);
		}

		// documentVersion 업데이트
		documentVersion.updateVersion(newDocument);
		documentVersionRepository.save(documentVersion);

		// 최근 수정 문서: redis 저장
		List<Object> docIds = redisService.getUpdatedListValue(userNum);
		if (docIds.contains(docUpdateReqDto.getId().intValue())) {
			redisService.removeUpdatedListValue(userNum, docUpdateReqDto.getId());
		}
		redisService.setUpdatedListValue(userNum, document.getId());
		redisService.setUpdatedListValue(userNum, newDocument.getId());

		// opens search 인덱싱
		EsDocument esDocument = EsDocument.toEsDocument(newDocument);
		documentSearchService.indexDocuments(esDocument);

		// 문서 업데이트 후 Kafka에 이벤트 전송
		String departmentId = document.getUser().getDepartment().getId().toString();
		String userName = document.getUser().getName();
		kafkaProducer.sendDocumentUpdateEvent("document-events", document.getFileName(), userName, departmentId, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));


		return newDocument;
	}

	// 문서 버전 rollback
	public void rollbackDoc(Long id) {
		Document document = documentRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("문서가 존재하지 않습니다."));
		document.revertDoc();

		List<Document> documents = documentRepository.findByDocumentVersionAndIdGreaterThan(document.getDocumentVersion(), id);
		for (Document doc : documents) {
			doc.softDelete();
			doc.updateStatus();
			documentRepository.save(doc);
		}

		// 문서 롤백 후 Kafka에 이벤트 전송
		String departmentId = document.getUser().getDepartment().getId().toString();
		String userName = document.getUser().getName();
		kafkaProducer.sendDocumentRollBackEvent("document-events", document.getFileName(), userName, departmentId, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));

	}

	// 	문서 히스토리 조회
	public List<DocHistoryResDto> getDocumentVersions(Long id) {
		Document document = documentRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("문서가 존재하지 않습니다."));

		List<DocHistoryResDto> versions = new ArrayList<>();

		DocumentVersion documentVersion = document.getDocumentVersion();
		List<Document> documents = documentRepository.findAllByDocumentVersion(documentVersion);
		for (Document doc : documents) {
			versions.add(doc.fromHistoryEntity());
		}
		return versions;
	}

	// 문서 삭제 -> 해당 문서만 삭제
	public void deleteDocument(Long id) {
		Document document = documentRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("문서가 존재하지 않습니다."));
		document.softDelete();
		documentSearchService.deleteDocument(id.toString());
		documentRepository.save(document);
	}

	// 모든 태그 조회
	public List<String> getAllTypeNames() {
		return null;
	}

	// 태그 추가
	public Long addTag(DocTagReqDto docTagReqDto) throws IOException {
		if (tagRepository.existsByTagName(docTagReqDto.getTagName())) {
			throw new IOException("이미 해당 이름의 태그가 존재합니다.");
		}

		tagRepository.save(
				Tag.builder().tagName(docTagReqDto.getTagName()).build());
		return tagRepository.count();
	}

	// 타입별 리스트 조회 -> 부서별
	// public List<DocListResDto> getDocByType(Long id) {
	// 	// DocumentType documentType = documentTypeRepository.findByTypeName(docTypeListReqDto.getTypeName())
	// 	// 	.orElseThrow(() -> new RuntimeException("존재하지 않는 타입입니다."));
	// 	DocumentTag documentTag = documentTagRepository.findById(id)
	// 		.orElseThrow(() -> new RuntimeException("존재하지 않는 타입입니다."));
	// 	List<Document> documents = documentRepository.findAllByDocumentTypeAndStatus(documentType, "now");
	//
	// 	return documents.stream()
	// 		.map(Document::fromEntityList)
	// 		.collect(Collectors.toList());
	// }

	public List<String> getAllTags() {
		List<Tag> tags = tagRepository.findAll();
		return tags.stream()
				.map(Tag::getTagName)
				.collect(Collectors.toList());
	}
}
