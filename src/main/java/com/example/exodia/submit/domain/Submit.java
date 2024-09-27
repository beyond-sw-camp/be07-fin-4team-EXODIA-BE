package com.example.exodia.submit.domain;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Where;

import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.common.domain.DelYN;
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
	private Long department_id;

	@OneToMany(mappedBy = "submit", cascade = CascadeType.PERSIST)
	private List<SubmitLine> submitLines = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "del_yn", nullable = false)
	private DelYN delYn = DelYN.N;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public void updateStatus(SubmitStatus status, String reason) {
		this.submitStatus = status;
		this.reason = reason;
	}
}

