package com.example.exodia.document.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.DeleteResponse;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.transport.endpoints.BooleanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.exodia.document.domain.Document;
import com.example.exodia.document.domain.EsDocument;
import com.example.exodia.document.dto.DocListResDto;
import com.example.exodia.document.dto.DocumentFilterDto;
import com.example.exodia.document.dto.DocumentSearchDto;
import com.example.exodia.document.repository.DocumentRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DocumentSearchService {

	private final OpenSearchClient openSearchClient;
	private static final String INDEX_NAME = "exodia_indexing_doc";
	private final DocumentRepository documentRepository;

	public DocumentSearchService(OpenSearchClient openSearchClient, DocumentRepository documentRepository) {
		this.openSearchClient = openSearchClient;
		this.documentRepository = documentRepository;
	}

	@PostConstruct
	public void init() {
		try {
			ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(INDEX_NAME));
			BooleanResponse existsResponse = openSearchClient.indices().exists(existsRequest);

			if (!existsResponse.value()) {
				createIndex();
			} else {
				log.info("인덱스가 이미 존재합니다: {}", INDEX_NAME);
			}
		} catch (Exception e) {
			log.error("인덱스 생성 중 오류 발생: ", e);
		}
	}

	// 인덱스 생성
	@Transactional
	public void createIndex() {
		try {
			CreateIndexRequest request = CreateIndexRequest.of(builder -> builder.index(INDEX_NAME));
			openSearchClient.indices().create(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//OpenSearch에 인덱싱(저장)
	@Transactional
	public void indexDocuments(EsDocument esDocument) {
		try {
			IndexRequest<EsDocument> indexRequest = IndexRequest.of(builder ->
				builder.index(INDEX_NAME)
					.id(esDocument.getId().toString())
					.document(esDocument)
			);
			IndexResponse response = openSearchClient.index(indexRequest);
			// System.out.println("OpenSearch에 인덱싱 : " + response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// OpenSearch에서 검색
	@Transactional
	public Page<EsDocument> searchDocuments(DocumentSearchDto documentSearchDto, int page, int size) {
		List<EsDocument> documents = new ArrayList<>();
		try {
			SearchRequest request = SearchRequest.of(searchRequest ->
				searchRequest.index(INDEX_NAME)
					.from(page * size)
					.size(size)
					.query(query -> query
						.bool(bool -> {
								String searchType = documentSearchDto.getSearchType();
								String keyword = "*" + documentSearchDto.getKeyword() + "*";
								// 작성자명에서 검색
								if ("userName".equals(searchType)) {
									bool.should(should -> should
										.wildcard(wildcard -> wildcard
											.field("userName")
											.value(keyword)
										)
									);
								} else if ("title".equals(searchType)) {
									// 제목에서 검색
									bool.should(should -> should
										.wildcard(wildcard -> wildcard
											.field("fileName")
											.value(keyword)
										)
									);
								} else if ("description".equals(searchType)) {
									// 설명에서 검색
									bool.should(should -> should
										.wildcard(wildcard -> wildcard
											.field("description")
											.value(keyword)
										)
									);
								} else {
									// 전체에서 검색하면 -> 파일 이름, 상세에서 검색
									bool.should(should -> should
										.wildcard(wildcard -> wildcard
											.field("fileName")
											.value(keyword)
										)
									);
									bool.should(should -> should
										.wildcard(wildcard -> wildcard
											.field("description")
											.value(keyword)
										)
									);
								}
								return bool;
							}

						)
					)
			);

			// 검색 결과
			SearchResponse<EsDocument> response = openSearchClient.search(request, EsDocument.class);
			List<Hit<EsDocument>> hits = response.hits().hits();

			System.out.println("hits.size() : " + hits.size());
			for (Hit<EsDocument> hit : hits) {
				EsDocument esDocument = hit.source();
				documents.add(esDocument);
			}
			Pageable pageable = PageRequest.of(page, size);
			long totalHits = response.hits().total().value();  // Total number of hits for pagination

			return new PageImpl<>(documents, pageable, totalHits);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Page.empty();
	}

	// OpenSearch에서 필터링
	@Transactional
	public Page<EsDocument> filterDocuments(DocumentFilterDto documentFilterDto, int page, int size) {
		List<EsDocument> documents = new ArrayList<>();
		try {
			SearchRequest request = SearchRequest.of(searchRequest ->
				searchRequest.index(INDEX_NAME)
					.from(page * size)
					.size(size)
					.query(query -> query
						.bool(bool -> {
								String filterType = documentFilterDto.getSearchType();
								String keyword = documentFilterDto.getKeyword();
								bool.filter(filter -> filter
									.term(term -> term.field(filterType).value(FieldValue.of(keyword)))
								);
								return bool;
							}
						)
					)
			);

			// 검색 결과
			SearchResponse<EsDocument> response = openSearchClient.search(request, EsDocument.class);
			List<Hit<EsDocument>> hits = response.hits().hits();

			System.out.println("hits.size() : " + hits.size());
			for (Hit<EsDocument> hit : hits) {
				EsDocument esDocument = hit.source();
				documents.add(esDocument);
			}
			Pageable pageable = PageRequest.of(page, size);
			long totalHits = response.hits().total().value();  // Total number of hits for pagination

			return new PageImpl<>(documents, pageable, totalHits);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Page.empty();
	}

	// 삭제
	@Transactional
	public void deleteDocument(String id) {
		try {
			DeleteRequest deleteRequest = DeleteRequest.of(builder ->
				builder.index(INDEX_NAME)
					.id(id));
			DeleteResponse response = openSearchClient.delete(deleteRequest);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 기본 쿼리 검색 (전체에서 검색)
	public List<DocListResDto> searchDocumentsQuery(String keyword) {
		List<Document> docs = documentRepository.searchByKeyword(keyword);
		List<DocListResDto> docListResDtos = new ArrayList<>();
		for (Document doc : docs) {
			docListResDtos.add(doc.fromEntityList());
		}
		return docListResDtos;
	}

}
