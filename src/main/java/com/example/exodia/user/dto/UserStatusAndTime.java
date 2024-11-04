package com.example.exodia.user.dto;

import java.time.LocalDateTime;

import com.example.exodia.user.domain.NowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusAndTime {
	private String name;
	private String userNum;
	private String positionName;
	private Long positionId;
	private String departmentName;
	private String profileImage;
	private NowStatus nowStatus;
	private LocalDateTime inTime;
	private LocalDateTime outTime;

}
