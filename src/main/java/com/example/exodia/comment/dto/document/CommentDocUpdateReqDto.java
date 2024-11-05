package com.example.exodia.comment.dto.document;

import java.time.LocalDateTime;

import com.example.exodia.comment.domain.CommentDoc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDocUpdateReqDto {
	private Long commentId;
	private String userNum;
	private String contents;
}
