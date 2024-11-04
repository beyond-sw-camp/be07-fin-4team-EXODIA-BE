package com.example.exodia.evalutionFrame.subevalution.dto;

import java.time.LocalDateTime;

import com.example.exodia.user.domain.NowStatus;
import com.example.exodia.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationResDto {
	private String name;
	private String userNum;
	private String positionName;
	private Long positionId;
	private String departmentName;
	private Long departmentId;
}
