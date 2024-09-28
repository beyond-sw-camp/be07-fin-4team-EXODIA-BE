package com.example.exodia.document.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocDetailResDto {
	private Long id;
	private String fileName;
	private String fileExtension;
	private String documentType;
	private String userName;
	private String description;	// 설명
	private LocalDateTime updatedAt;
}
