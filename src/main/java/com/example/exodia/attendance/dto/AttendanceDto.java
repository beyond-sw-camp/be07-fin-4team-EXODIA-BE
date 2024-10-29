package com.example.exodia.attendance.dto;

import com.example.exodia.attendance.domain.Attendance;
import com.example.exodia.user.domain.NowStatus;
import com.example.exodia.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDto {
    private Long id;
    private LocalDateTime inTime;
    private LocalDateTime outTime;
    private NowStatus nowStatus;
    private Long userId;


    public static AttendanceDto fromEntity(User user, Attendance attendance) {
        return AttendanceDto.builder()
                .id(attendance.getId())
                .inTime(attendance.getInTime())
                .outTime(attendance.getOutTime())
                .nowStatus(attendance.getNowStatus())
                .userId(attendance.getUser().getId())
                .nowStatus(user.getN_status())
                .build();
    }
}
