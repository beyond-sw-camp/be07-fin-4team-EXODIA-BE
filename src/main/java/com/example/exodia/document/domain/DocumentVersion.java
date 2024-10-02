package com.example.exodia.document.domain;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.units.qual.A;
import org.hibernate.annotations.Where;

import com.example.exodia.common.domain.DelYN;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class DocumentVersion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "document_type_id", nullable = false)
	private DocumentType documentType;

	private Long document_id;

	@Enumerated(EnumType.STRING)
	@Column(name = "del_yn", nullable = false)
	private DelYN delYn = DelYN.N;

	public static DocumentVersion toEntity(Document document) {
		return DocumentVersion.builder()
			.document_id(document.getId())
			.documentType(document.getDocumentType())
			.delYn(DelYN.N)
			.build();
	}

	// public DocumentVersion updateEntity(Long id, String version) {
	// 	return DocumentVersion.builder()
	// 		.version(String.valueOf(Integer.parseInt(version) + 1))
	// 		.documentType(documentType)
	// 		.delYn(DelYN.N)
	// 		.build();
	// }

	public void updateVersion(Document document) {
		this.document_id = document.getId();
	}
}
