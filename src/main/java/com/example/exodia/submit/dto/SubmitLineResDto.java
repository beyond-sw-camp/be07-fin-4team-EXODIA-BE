package com.example.exodia.submit.dto;

import com.example.exodia.submit.domain.Submit;
import com.example.exodia.submit.domain.SubmitStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitLineResDto {
	private String userName;
	private String profileImage;
	private String positionName;
	private SubmitStatus submitStatus;

}
