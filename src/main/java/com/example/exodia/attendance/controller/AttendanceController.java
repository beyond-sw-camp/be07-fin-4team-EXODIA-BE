package com.example.exodia.attendance.controller;

import com.example.exodia.attendance.AttendanceService;
import com.example.exodia.attendance.domain.Attendance;
import com.example.exodia.attendance.dto.AttendanceSaveDto;
import com.example.exodia.attendance.dto.AttendanceUpdateDto;
import com.example.exodia.attendance.dto.WeeklySumDto;
import com.example.exodia.common.dto.CommonResDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/work-in")
    //@Operation(summary = "[일반 사용자] 출근 시간 기록 API")
    public ResponseEntity<?> workIn(@RequestBody AttendanceSaveDto dto) {
        Attendance attendance = attendanceService.workIn(dto);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.CREATED, " 출근 시간이 기록되었습니다.", attendance.getId()), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/work-out")
    //@Operation(summary = "[일반 사용자] 퇴근 시간 기록 API")
    public ResponseEntity<?> workOut(@RequestBody AttendanceUpdateDto dto) {
        Attendance attendance = attendanceService.workOut(dto);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.CREATED, " 퇴근 시간이 기록되었습니다.", attendance.getId()), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/weekly-summary")
    //@Operation(summary = "[일반 사용자] 주간 근무 시간 요약 API")
    public ResponseEntity<?> getWeeklySummary() {
        WeeklySumDto sumDto = attendanceService.getWeeklySum();
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "주간 근무 시간 정보 조회 완료", sumDto), HttpStatus.OK);
    }


}
