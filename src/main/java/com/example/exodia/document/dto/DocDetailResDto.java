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
	private String fileName;
	private String fileExtension;
	private LocalDateTime createdAt;
	// private User user;
	private String description;	// 설명
}
