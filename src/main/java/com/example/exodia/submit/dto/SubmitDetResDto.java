package com.example.exodia.submit.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitDetResDto {
	private Long id;
	private String userName;
	private String department;
	private String contents;
	private String submitStatus;
	private String submitType;
	private List<SubmitLineResDto> submitUserDtos;
	private String rejectReason;
	private LocalDateTime submitTime;
}
