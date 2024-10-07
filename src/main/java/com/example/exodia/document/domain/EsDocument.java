package com.example.exodia.document.domain;

import java.time.LocalDateTime;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

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

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
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
