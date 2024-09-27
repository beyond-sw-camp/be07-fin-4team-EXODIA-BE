package com.example.exodia.document.domain;

import org.hibernate.annotations.Where;

import com.example.exodia.common.domain.DelYN;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

	private String version;

	@OneToOne
	@JoinColumn(name = "document_type_id", nullable = false)
	private DocumentType documentType;

	@Enumerated(EnumType.STRING)
	@Column(name = "del_yn", nullable = false)
	private DelYN delYn = DelYN.N;

	public static DocumentP toEntity(Long id, DocumentType documentType, String version) {
		return DocumentP.builder()
			.version(version)
			.documentType(documentType)
			.delYn(DelYN.N)
			.build();
	}

	public DocumentP updateEntity(Long id, String version) {
		return DocumentP.builder()
			.version(String.valueOf(Integer.parseInt(version) + 1))
			.documentType(documentType)
			.delYn(DelYN.N)
			.build();
	}
}
