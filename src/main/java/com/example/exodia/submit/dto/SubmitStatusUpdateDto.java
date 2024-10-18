package com.example.exodia.submit.dto;

import com.example.exodia.board.domain.Board;
import com.example.exodia.board.domain.Category;
import com.example.exodia.common.domain.DelYN;
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
public class SubmitStatusUpdateDto {
	private Long submitId;
	private SubmitStatus status;
	private String reason;

	public void chkReason(){
		if (status == SubmitStatus.REJECT && reason == null) {
			throw new IllegalArgumentException("반려 사유를 입력하세요.");
		}
	}

	public Board toEntity(Submit submit) {
		return Board.builder()
			.title("결재 선택됨 그래서 게시글에 올라감")
			.content("부고문자랑 통일해야함")
			.category(Category.FAMILY_EVENT)
			.delYn(DelYN.N)
			.isPinned(false)
			.user(submit.getUser())
			.build();
	}
}

