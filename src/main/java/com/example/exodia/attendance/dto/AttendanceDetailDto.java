package com.example.exodia.attendance.dto;

import com.example.exodia.attendance.domain.Attendance;
import com.example.exodia.attendance.domain.DayStatus;
import com.example.exodia.user.domain.NowStatus;

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
    private double overtimeHours;
    // private DayStatus dayStatus;
    private NowStatus nowStatus;

//    public static AttendanceDetailDto fromEntity(Attendance attendance) {
//        double hoursWorked = 0;
//        if (attendance.getInTime() != null && attendance.getOutTime() != null) {
//            hoursWorked = Duration.between(attendance.getInTime(), attendance.getOutTime()).toHours();
//        }
//
//        return new AttendanceDetailDto(attendance.getInTime(), attendance.getOutTime(), hoursWorked);
//    }

    public static AttendanceDetailDto fromEntity(Attendance attendance) {
        return AttendanceDetailDto.builder()
            .inTime(attendance.getInTime())
            .outTime(attendance.getOutTime())
            // .dayStatus(attendance.getDayStatus())
            .nowStatus(attendance.getNowStatus())
            .overtimeHours(attendance.getHoursWorked())
            .build();
    }

    public AttendanceDetailDto(LocalDateTime inTime, LocalDateTime outTime, double hoursWorked, double overtimeHours) {
        this.inTime = inTime;
        this.outTime = outTime;
        this.hoursWorked = hoursWorked;
        this.overtimeHours = overtimeHours;
    }
}
