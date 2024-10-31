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
	private String userName;
	private String userNum;
	private String profileImage;
	private String positionName;
	private NowStatus nowStatus;
	private LocalDateTime inTime;
	private LocalDateTime outTime;

}
