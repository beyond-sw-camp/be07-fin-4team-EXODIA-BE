package com.example.exodia.comment.dto.document;

import com.example.exodia.comment.domain.CommentDoc;
import com.example.exodia.document.domain.Document;
import com.example.exodia.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDocSaveReqDto {

	private Long documentId;
	private String userNum;
	private String contents;

	public CommentDoc toEntity(User user, Document document) {
		return CommentDoc.builder()
			.user(user)
			.document(document)
			.contents(this.contents)
			.build();
	}
}
