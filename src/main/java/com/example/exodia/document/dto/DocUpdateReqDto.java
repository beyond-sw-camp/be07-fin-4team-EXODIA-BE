package com.example.exodia.document.dto;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.document.domain.Document;
import com.example.exodia.document.domain.DocumentVersion;
import com.example.exodia.document.domain.DocumentType;
import com.example.exodia.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocUpdateReqDto {
	private Long id;
	private String typeName;
	private String description;	// 설명

	public Document toEntity(DocUpdateReqDto docUpdateReqDto, Document document, String fileName, String fileDownloadUrl,
		DocumentType documentType){
		return Document.builder()
			.fileName(fileName)
			.filePath(fileDownloadUrl)
			.documentVersion(document.getDocumentVersion())
			.documentType(documentType)
			.user(document.getUser())
			.build();
	}
}
