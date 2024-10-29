package com.example.exodia.document.domain;

import org.hibernate.annotations.Where;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.department.domain.Department;
import com.example.exodia.document.dto.DocTagReqDto;
import com.example.exodia.document.dto.TagListResDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Tag {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String tagName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_id", nullable = false)
	private Department department;

	public DocumentTag toEntity(Document document){
		return DocumentTag.builder()
			.document(document)
			.tagName(this.getTagName())
			.build();
	}

	public TagListResDto fromEntity(){
		return TagListResDto.builder()
			.id(this.id)
			.tagName(this.tagName)
			.build();
	}
}
