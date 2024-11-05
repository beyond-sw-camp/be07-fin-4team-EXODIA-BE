package com.example.exodia.user.dto;

import java.time.LocalDateTime;

import com.example.exodia.attendance.domain.Attendance;
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
public class UserStatusAndTime {
	private String name;
	private String userNum;
	private String positionName;
	private String departmentName;
	private String profileImage;
	private NowStatus nowStatus;
	private LocalDateTime inTime;
	private LocalDateTime outTime;

}
