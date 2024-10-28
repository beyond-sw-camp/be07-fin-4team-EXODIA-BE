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
public class DocHistoryResDto {
	private Long id;
	private String fileName;
	private String userName;
	private String userProfileImage;
	private String description;
	private LocalDateTime updatedAt;
}
