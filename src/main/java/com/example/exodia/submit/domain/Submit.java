package com.example.exodia.submit.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Where;

import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.department.domain.Department;
import com.example.exodia.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Where(clause = "del_yn = 'N'")
public class Submit extends BaseTimeEntity {
	@Id
	@Column(nullable = false, length = 12)
	private String id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SubmitStatus submitStatus;

	@Column(nullable = false)
	private String submitType;

	@Column(nullable = false)
	private String contents;

	@Enumerated(EnumType.STRING)
	@Column(name = "del_yn", nullable = false)
	private DelYN delYn = DelYN.N;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "department_id", nullable = false)
	private Department department;
}
