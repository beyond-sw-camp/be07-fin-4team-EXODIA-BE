package com.example.exodia.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/* 주차별 기록 DTO */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySumDto {
    private double totalHours; // 주차별 누적 근무시간
    private double overtimeHours; // 주차별 초과 근무시간
}
