package com.example.exodia.document.domain;

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
	private String id;

	@Field(type = FieldType.Text)
	private String fileName;

	@Field(type = FieldType.Text)
	private String description;

	// @Field(type = FieldType.Text)
	// private String content;

	public static EsDocument toEsDocument(Document document) {
		return EsDocument.builder()
			.id(document.getId().toString())
			.fileName(document.getFileName())
			.description(document.getDescription())
			.build();
	}
}
