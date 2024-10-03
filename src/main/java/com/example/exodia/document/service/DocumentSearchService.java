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
import org.springframework.stereotype.Service;

import com.example.exodia.document.domain.EsDocument;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DocumentSearchService {

	private final OpenSearchClient openSearchClient;
	private static final String INDEX_NAME = "doc";

	public DocumentSearchService(OpenSearchClient openSearchClient) {
		this.openSearchClient = openSearchClient;
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
					.id(esDocument.getId())
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
									.field("descrption")
									.value("*" + keyword + "*")
								)
							)
						)
					)
			);

			// 검색 결과
			SearchResponse<EsDocument> response = openSearchClient.search(request, EsDocument.class);
			List<Hit<EsDocument>> hits = response.hits().hits();
			for (Hit<EsDocument> hit : hits) {
				documents.add(hit.source());
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
