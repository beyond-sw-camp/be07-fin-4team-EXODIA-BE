package com.example.exodia.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAttendanceDto {
    private int weekNumber;
    private LocalDate startOfWeek;
    private LocalDate endOfWeek;
    private Map<String, DailyAttendanceDto> days;

}

