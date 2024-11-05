package com.example.exodia.attendance.controller;

import com.example.exodia.attendance.dto.*;
import com.example.exodia.attendance.service.AttendanceService;
import com.example.exodia.attendance.domain.Attendance;
import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.dto.UserAttendanceDto;
import com.example.exodia.user.dto.UserStatusAndTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    //@Operation(summary = "[일반 사용자] 주차별 근무 시간 정보 조회 API")
    public ResponseEntity<?> getWeeklySummary(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<WeeklySumDto> weeklySummaries = attendanceService.getWeeklySummaries(start, end);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "주차별 근무 시간 정보 조회 완료", weeklySummaries), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/weekly-details")
    public ResponseEntity<?> getWeeklyDetails(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        Map<String, List<AttendanceDetailDto>> weeklyDetails = attendanceService.getWeeklyDetailsWithOvertime(start, end);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "주차별 상세 근무 시간 조회 완료", weeklyDetails), HttpStatus.OK);
    }


    @GetMapping("/weekly")
    public ResponseEntity<List<WeeklyAttendanceDto>> getWeeklyAttendance(@RequestParam("year") int year) {
        // 서비스 호출 시 로그인된 사용자 정보를 자동으로 처리
        List<WeeklyAttendanceDto> weeklyAttendance = attendanceService.getWeeklyAttendance(year);
        return ResponseEntity.ok(weeklyAttendance);
    }

    /* 당일 출 퇴근 정보 조회 */
    @GetMapping("/today")
    public ResponseEntity<?> getTodayAttendance() {
        DailyAttendanceDto dto = attendanceService.getTodayAttendance();
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "오늘 출퇴근 기록 조회 성공", dto), HttpStatus.OK);
    }

    // 같은 부서 출근 상태 조회
    @GetMapping("/department/status")
    public ResponseEntity<Map<String, List<UserAttendanceDto>>> getDepartmentUsersAttendanceStatus() {
        try {
            // 서비스에서 출근 상태 조회
            Map<String, List<User>> attendanceStatusMap = attendanceService.getDepartmentUsersAttendanceStatus();

            Map<String, List<UserAttendanceDto>> responseMap = new HashMap<>();
            responseMap.put("출근한 사람들", attendanceStatusMap.get("출근한 사람들").stream()
                    .map(UserAttendanceDto::fromEntity)
                    .collect(Collectors.toList()));
            responseMap.put("출근하지 않은 사람들", attendanceStatusMap.get("출근하지 않은 사람들").stream()
                    .map(UserAttendanceDto::fromEntity)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/department/list")
    public ResponseEntity<?> getTeamAttendance(@PageableDefault(size = 6)  Pageable pageable) {
        try {
            return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "팀원 근태 정보 조회 성공", attendanceService.getTodayRecords(pageable)));
        } catch (IOException e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/meeting-in")
    public ResponseEntity<?> inMeetingStatus() {
        try {
            return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "자리비움으로 변경 성공", attendanceService.inMeetingStatus().getNowStatus()));
        } catch (IOException e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/meeting-out")
    public ResponseEntity<?> outMeetingStatus() {
        try {
            return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "복귀 성공", attendanceService.outMeetingStatus().getNowStatus()));
        } catch (IOException e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
}




