package com.example.exodia.document.domain;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.Where;

import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.document.dto.DocDetailResDto;
import com.example.exodia.document.dto.DocHistoryResDto;
import com.example.exodia.document.dto.DocListResDto;
import com.example.exodia.user.domain.User;

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


	// @Column(nullable = false)
	// @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	// private LocalDateTime saveDate;

	// @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	// @Column(name = "updated_at")
	// private LocalDateTime updatedAt;	// 최근 수정 시간
	//
	// @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	// @Column(name = "viewed_at")
	// private LocalDateTime viewedAt;	// 최근 열람 시간

	@ManyToOne
	@JoinColumn(name = "document_type", nullable = false)
	private DocumentType documentType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_num", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "document_version")
	private DocumentVersion documentVersion;

	//
	// @ManyToOne
	// @JoinColumn(name = "department_id", nullable = false)
	// private Department department;

	public DocDetailResDto fromEntity(){
		return DocDetailResDto.builder()
			.id(this.id)
			.fileName(this.fileName)
			.fileExtension(this.fileName.substring(fileName.lastIndexOf(".") + 1))
			.documentType(this.getDocumentType().getTypeName())
			.userName(this.user.getName())
			.description(this.description)
			.createAt(this.getCreatedAt())
			.build();
	}

	public DocListResDto fromEntityList(){
		return DocListResDto.builder()
			.id(this.id)
			.fileName(this.fileName)
			.type(this.documentType.getTypeName())
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

	public void updateStatus() {
		this.status = "";
	}

	public void revertDoc(){
		this.setDelYn(DelYN.N);
		this.setDeletedAt(null);
		this.status = "now";
	}

}


