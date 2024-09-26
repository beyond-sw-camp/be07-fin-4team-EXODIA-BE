package com.example.exodia.submit.dto;

import com.example.exodia.submit.domain.SubmitStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitStatusUpdateDto {
	private Long submitId;
	private SubmitStatus status;
	private String reason;

	public void chkReason(){
		if (status == SubmitStatus.REJECT && reason == null) {
			throw new IllegalArgumentException("반려 사유를 입력하세요.");
		}
	}
}

