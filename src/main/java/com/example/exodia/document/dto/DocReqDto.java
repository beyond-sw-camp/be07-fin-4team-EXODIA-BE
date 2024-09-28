package com.example.exodia.document.dto;

import org.springframework.web.multipart.MultipartFile;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.document.domain.DocumentC;
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


	public DocumentC toEntity(DocReqDto docReqDto, User user, String fileName, String fileDownloadUrl, DocumentType documentType) {
		return DocumentC.builder()
			.fileName(fileName)
			.filePath(fileDownloadUrl)
			.documentType(documentType)
			.description(docReqDto.description)
			.user(user)
			.documentP(null)
			.delYn(DelYN.N)
			.build();
	}
}
