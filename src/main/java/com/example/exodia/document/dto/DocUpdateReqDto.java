package com.example.exodia.document.dto;

import java.time.LocalDateTime;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.document.domain.DocumentC;
import com.example.exodia.document.domain.DocumentP;
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

	public DocumentC updatetoEntity(DocUpdateReqDto docUpdateReqDto, DocumentP documentP, User user, String fileName, String fileDownloadUrl,
		DocumentType documentType){
		return DocumentC.builder()
			.fileName(fileName)
			.filePath(fileDownloadUrl)
			.documentP(documentP)
			.documentType(documentType)
			.user(user)
			.delYn(DelYN.N)
			.build();
	}
}
