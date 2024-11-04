package com.example.exodia.evalutionavg.dto;

import com.example.exodia.evalutionFrame.subevalution.domain.SubEvalution;
import com.example.exodia.evalutionFrame.subevalution.dto.SubEvalutionWithUserDetailsDto;
import com.example.exodia.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationAvgResDto {

	private Long subEvalutionId;
	private String subEvalutionContent;
	private String midCategoryName;
	private String bigCategoryName;
	private String userName;
	private String userPosition;
	private double avgScore;
}
