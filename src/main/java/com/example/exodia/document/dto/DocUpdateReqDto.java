package com.example.exodia.document.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.document.domain.Document;
import com.example.exodia.document.domain.DocumentVersion;
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
	private String description;	// 설명
	private List<String> tags;

	public Document toEntity(DocUpdateReqDto docUpdateReqDto, Document document, String fileName, String fileDownloadUrls){
		return Document.builder()
			.fileName(fileName)
			.filePath(fileDownloadUrls)
			.documentVersion(document.getDocumentVersion())
			.user(document.getUser())
			.status("now")
			.tags(new ArrayList<>())
			.description(docUpdateReqDto.getDescription())
			.build();
	}

}
