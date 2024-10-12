package com.example.exodia.comment.domain;

import org.hibernate.annotations.Where;

import com.example.exodia.comment.dto.document.CommentDocListResDto;
import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.document.domain.Document;
import com.example.exodia.user.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Where(clause = "del_yn = 'N'")
public class CommentDoc extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "document_id", nullable = false)
	private Document document;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_num", nullable = false)
	private User user;

	@Column(nullable = false, length = 500)
	private String contents;

	public CommentDocListResDto fromEntity(){
			return CommentDocListResDto.builder()
				.id(this.id)
				.contents(this.user.getUserNum())
				.contents(this.contents)
				.build();
	}
}
