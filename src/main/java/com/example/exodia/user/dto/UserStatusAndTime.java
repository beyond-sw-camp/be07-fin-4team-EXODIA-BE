package com.example.exodia.user.dto;

import java.time.LocalDateTime;

import com.example.exodia.user.domain.NowStatus;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusAndTime {

	private String userName;
	private String userNum;
	private String profileImage;
	private NowStatus nowStatus;
	private LocalDateTime inTime;
	private LocalDateTime outTime;

}
