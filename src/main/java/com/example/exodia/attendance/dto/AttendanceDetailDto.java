package com.example.exodia.attendance.dto;

import com.example.exodia.attendance.domain.Attendance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDetailDto {
    private LocalDateTime inTime;
    private LocalDateTime outTime;
    private double hoursWorked;

    public static AttendanceDetailDto fromEntity(Attendance attendance) {
        double hoursWorked = 0;
        if (attendance.getInTime() != null && attendance.getOutTime() != null) {
            hoursWorked = Duration.between(attendance.getInTime(), attendance.getOutTime()).toHours();
        }

        return new AttendanceDetailDto(attendance.getInTime(), attendance.getOutTime(), hoursWorked);
    }
}
