package com.example.exodia.document.domain;

import java.time.LocalDateTime;

import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.annotations.Field;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@org.springframework.data.elasticsearch.annotations.Document(indexName = "document")
public class EsDocument {

	@Id
	private Long id;

	@Field(type = FieldType.Text)
	private String fileName;

	@Field(type = FieldType.Text)
	private String description;

	@Field(type = FieldType.Text)
	private String type;

	@Field(type = FieldType.Text)
	private String departmentName;

	@Field(type = FieldType.Text)
	private String userName;

	@Field(type = FieldType.Text)
	private LocalDateTime createdAt;

	public static EsDocument toEsDocument(Document document) {
		return EsDocument.builder()
			.id(document.getId())
			.fileName(document.getFileName())
			.type(document.getDocumentType().getTypeName())
			.userName(document.getUser().getName())
			.createdAt(document.getCreatedAt())
			.description(document.getDescription())
			.build();
	}
}
