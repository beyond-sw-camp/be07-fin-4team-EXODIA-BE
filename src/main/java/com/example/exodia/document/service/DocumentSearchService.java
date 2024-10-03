package com.example.exodia.document.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensearch.client.opensearch.OpenSearchClient;
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
import org.springframework.stereotype.Service;

import com.example.exodia.document.domain.Document;
import com.example.exodia.document.domain.EsDocument;
import com.example.exodia.document.dto.DocListResDto;
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
	public void createIndex() {
		try {
			CreateIndexRequest request = CreateIndexRequest.of(builder -> builder.index(INDEX_NAME));
			openSearchClient.indices().create(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//OpenSearch에 인덱싱(저장)
	public void indexDocuments(EsDocument esDocument) {
		try {
			IndexRequest<EsDocument> indexRequest = IndexRequest.of(builder ->
				builder.index(INDEX_NAME)
					.id(esDocument.getId().toString())
					.document(esDocument)
			);
			IndexResponse response = openSearchClient.index(indexRequest);
			System.out.println("OpenSearch에 인덱싱 : " + response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// OpenSearch에서 검색
		public List<EsDocument> searchDocuments(String keyword) {
			List<EsDocument> documents = new ArrayList<>();
			try {
				SearchRequest request = SearchRequest.of(searchRequest ->
					searchRequest.index(INDEX_NAME)
						.query(query -> query
							.bool(bool -> bool
								.should(should -> should
									.wildcard(wildcard -> wildcard
										.field("fileName")
										.value("*" + keyword + "*")
									)
								)
								.should(should -> should
									.wildcard(wildcard -> wildcard
										.field("description")
										.value("*" + keyword + "*")
									)
								)
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
			} catch (IOException e) {
				e.printStackTrace();
			}
			return documents;
		}

	// 삭제
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



	// 기본 검색
	public List<DocListResDto> searchDocumentsQuery(String keyword) {
		List<Document> docs = documentRepository.searchByKeyword(keyword);
		List<DocListResDto> docListResDtos = new ArrayList<>();
		for (Document doc : docs) {
			docListResDtos.add(doc.fromEntityList());
		}
		return docListResDtos;
	}

}
