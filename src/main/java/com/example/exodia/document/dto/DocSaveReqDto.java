package com.example.exodia.document.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.document.domain.Document;
import com.example.exodia.document.domain.DocumentTag;
import com.example.exodia.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocSaveReqDto {

	private String description;
	private List<String> tags;


	public Document toEntity(DocSaveReqDto docSaveReqDto, User user, String fileName, String fileDownloadUrl) {
		return Document.builder()
			.fileName(fileName)
			.filePath(fileDownloadUrl)
			.description(docSaveReqDto.description)
			.user(user)
			.departmentId(user.getDepartment().getId())
			.status("now")
			.documentVersion(null)
			.tags(new ArrayList<>())
			.build();
	}

	public static DocumentTag toTagEntity(String tagName, Document document) {
		return DocumentTag.builder()
			.tagName(tagName)
			.delYn(DelYN.N)
			.build();
	}
}
