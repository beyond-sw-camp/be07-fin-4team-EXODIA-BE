package com.example.exodia.submit.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Where;

import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.submit.dto.SubmitDetResDto;
import com.example.exodia.submit.dto.SubmitLineResDto;
import com.example.exodia.submit.dto.SubmitSaveReqDto;
import com.example.exodia.user.domain.User;

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
public class Submit extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SubmitStatus submitStatus;

	@Column(nullable = false)
	private String submitType;

	@Column(nullable = false)
	private String contents;

	@Column(nullable = true)
	private String reason;

	@Column(nullable = false)
	private boolean uploadBoard = false;	// 게시판에 등록 여부

	@Column(nullable = false)
	private Long department_id;

	@OneToMany(mappedBy = "submit", cascade = CascadeType.PERSIST)
	private List<SubmitLine> submitLines = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "user_num", referencedColumnName = "user_num", nullable = false)
	private User user;

	public void updateStatus(SubmitStatus status, String reason) {
		this.submitStatus = status;
		this.reason = reason;
		this.setUpdatedAt(LocalDateTime.now());
	}

	public SubmitDetResDto fromEntity(String departmentName, List<SubmitLineResDto> dtos){
		return SubmitDetResDto.builder()
			.id(this.id)
			.userName(this.getUser().getName())
			.department(departmentName)
			.contents(this.contents)
			.submitStatus(this.submitStatus.toString())
			.submitType(this.submitType)
			.submitUserDtos(dtos)
			.rejectReason(this.getReason())
			.submitTime(this.getCreatedAt())
			.build();
	}

	public String getUserNum() {
		return this.user.getUserNum();
	}
}

