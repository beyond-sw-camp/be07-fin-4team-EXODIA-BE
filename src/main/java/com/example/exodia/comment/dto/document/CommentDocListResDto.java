package com.example.exodia.comment.dto.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDocListResDto {
	private Long id;
	private String contents;
	private String userName;
}
