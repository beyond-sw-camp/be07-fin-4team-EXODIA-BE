package com.example.exodia.document.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.Where;

import com.example.exodia.chat.domain.ChatFile;
import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.document.dto.DocDetailResDto;
import com.example.exodia.document.dto.DocHistoryResDto;
import com.example.exodia.document.dto.DocListResDto;
import com.example.exodia.submit.domain.SubmitLine;
import com.example.exodia.user.domain.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class Document extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String fileName;

	// 다운로드 경로
	@Column(nullable = false, length = 2083)
	private String filePath;

	@Column(nullable = true, length = 2083)
	private String description;

	@Column(nullable = true)
	private String status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_num", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "document_version")
	private DocumentVersion documentVersion;

	@OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DocumentTag> tags = new ArrayList<>();

	public DocDetailResDto fromEntity(List<String> docTagName){
		return DocDetailResDto.builder()
			.id(this.id)
			.fileName(this.fileName)
			.fileExtension(this.fileName.substring(fileName.lastIndexOf(".") + 1))
			.userName(this.user.getName())
			.tags(docTagName)
			.description(this.description)
			.createAt(this.getCreatedAt())
			.build();
	}

	public DocListResDto fromEntityList(){
		return DocListResDto.builder()
			.id(this.id)
			.fileName(this.fileName)
			.departmentName(this.user.getDepartment().getName())
			.userName(this.user.getName())
			.createdAt(this.getCreatedAt())
			.build();
	}

	public DocHistoryResDto fromHistoryEntity() {
		return DocHistoryResDto.builder()
			.id(this.getId())
			.fileName(this.getFileName())
			.userName(this.getUser().getName())
			.description(this.description)
			.updatedAt(this.getUpdatedAt())
			.build();
	}

	public void updateDocumentVersion(DocumentVersion documentVersion) {
		this.documentVersion = documentVersion;
	}

	public void updateDocumentTags(List<DocumentTag> tags) {
		this.tags = tags;
	}

	public void updateStatus() {
		this.status = "";
	}

	public void revertDoc(){
		this.setDelYn(DelYN.N);
		this.setDeletedAt(null);
		this.status = "now";
	}

}


