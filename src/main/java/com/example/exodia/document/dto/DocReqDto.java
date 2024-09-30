package com.example.exodia.document.dto;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.document.domain.Document;
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
public class DocReqDto {

	private String typeName;
	private String description;


	public Document toEntity(DocReqDto docReqDto, User user, String fileName, String fileDownloadUrl, DocumentType documentType) {
		return Document.builder()
			.fileName(fileName)
			.filePath(fileDownloadUrl)
			.documentType(documentType)
			.description(docReqDto.description)
			.user(user)
			.documentVersion(null)
			.delYn(DelYN.N)
			.build();
	}
}
