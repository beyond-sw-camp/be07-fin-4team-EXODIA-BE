package com.example.exodia.document.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Where;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.document.dto.DocReqDto;

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
public class DocumentP {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, length = 12)
	private Long id;

	@OneToOne
	private DocumentType documentType;

	@Enumerated(EnumType.STRING)
	@Column(name = "del_yn", nullable = false)
	private DelYN delYn = DelYN.N;

	public static DocumentP toEntity(DocReqDto docReqDto, Long id) {
		return DocumentP.builder()
			.id(id)
			.delYn(DelYN.N)
			.documentType(docReqDto.getDocumentType())
			.build();
	}
}
