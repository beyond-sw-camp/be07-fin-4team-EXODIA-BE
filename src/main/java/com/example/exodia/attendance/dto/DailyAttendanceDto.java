package com.example.exodia.attendance.dto;

import com.example.exodia.attendance.domain.Attendance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyAttendanceDto {
    private LocalDateTime inTime;
    private LocalDateTime outTime;
    private double hoursWorked;

    public static DailyAttendanceDto fromEntity(Attendance attendance) {
        DailyAttendanceDto dto = new DailyAttendanceDto();
        dto.setInTime(attendance.getInTime());
        dto.setOutTime(attendance.getOutTime());
        dto.setHoursWorked(attendance.getHoursWorked());
        return dto;
    }
}
