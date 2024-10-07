package com.example.exodia.attendance.dto;

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

}
