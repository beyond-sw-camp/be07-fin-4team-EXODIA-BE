package com.example.exodia.submit.dto;

import java.time.LocalDateTime;

import org.springframework.security.core.context.SecurityContextHolder;

import com.example.exodia.submit.domain.Submit;
import com.example.exodia.submit.domain.SubmitLine;
import com.example.exodia.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitListResDto {
	private String userName;
	private String department;
	private String submitStatus;
	private String submitType;
	private String rejectReason;
	private LocalDateTime submitTime;


	public SubmitListResDto fromLineEntity(User user,  SubmitLine submitLine){
		return SubmitListResDto.builder()
			.userName(user.getUserNum())
			.department(user.getDepartment().getName())
			.submitStatus(submitLine.getSubmitStatus().toString())
			.submitType(submitLine.getSubmit().getSubmitType())
			.rejectReason(submitLine.getSubmit().getReason())
			.submitTime(submitLine.getCreatedAt())
			.build();
	}

	public SubmitListResDto fromEntity(Submit submit){
		return SubmitListResDto.builder()
			.userName(submit.getUser().getUserNum())
			.department(submit.getUser().getDepartment().getName())
			.submitStatus(submit.getSubmitStatus().toString())
			.submitType(submit.getSubmitType())
			.rejectReason(submit.getReason())
			.submitTime(submit.getCreatedAt())
			.build();
	}
}
