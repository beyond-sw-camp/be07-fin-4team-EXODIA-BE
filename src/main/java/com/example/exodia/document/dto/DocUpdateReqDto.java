package com.example.exodia.document.dto;

import java.time.LocalDateTime;

import com.example.exodia.document.domain.DocumentType;

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
}
