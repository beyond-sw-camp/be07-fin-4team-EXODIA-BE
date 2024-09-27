package com.example.exodia.submit.domain;

import org.hibernate.annotations.Where;

import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.common.domain.DelYN;

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
@Where(clause = "del_yn = 'N'")
public class SubmitLine extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SubmitStatus submitStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "del_yn", nullable = false)
	private DelYN delYn = DelYN.N;

	@Column(nullable = false)
	private String userNum;

	@Column(nullable = false)
	private Long department_id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "submit_id", nullable = false)
	private Submit submit;


	public void updateSubmit(Submit submit) {
		this.submit = submit;
	}

	public void updateStatus(SubmitStatus status) {
		this.submitStatus = status;
	}
}
