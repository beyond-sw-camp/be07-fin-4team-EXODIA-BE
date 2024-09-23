package com.example.exodia.attendance.service;

import com.example.exodia.attendance.domain.Attendance;
import com.example.exodia.attendance.dto.AttendanceSaveDto;
import com.example.exodia.attendance.dto.AttendanceUpdateDto;
import com.example.exodia.attendance.dto.WeeklySumDto;
import com.example.exodia.attendance.repository.AttendanceRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {
    @Autowired
    private final AttendanceRepository attendanceRepository;
    @Autowired
    private final UserRepository userRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    /*출근시간 기록 용*/
    @Transactional
    public Attendance workIn(AttendanceSaveDto dto) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

        Attendance attendance = dto.toEntity(user);
        return attendanceRepository.save(attendance);
    }

    /*퇴근시간 기록 용*/
    @Transactional
    public Attendance workOut(AttendanceUpdateDto dto) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));
        Attendance attendance = attendanceRepository.findTopByUserAndOutTimeIsNull(user).orElseThrow(() -> new RuntimeException("출근 기록이 존재하지 않습니다"));

        dto.updateEntity(attendance);
        return attendanceRepository.save(attendance);
    }

    public WeeklySumDto getWeeklySum() {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

        LocalDateTime startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);

        List<Attendance> weeklyAttendance = attendanceRepository.findAllByUserAndWeek(user, startOfWeek, endOfWeek);
        return calculateWeeklySum(weeklyAttendance);
    }

    public WeeklySumDto calculateWeeklySum(List<Attendance> weeklyAttendance) {
        WeeklySumDto weeklySumDto = new WeeklySumDto();
        double totalHours = 0;
        double overallHours = 0;

        for (Attendance attendance : weeklyAttendance) {
            if (attendance.getInTime() != null && attendance.getOutTime() != null) {
                double workHour = Duration.between(attendance.getInTime(), attendance.getOutTime()).toHours();
                totalHours += workHour -1; // 점심시간 빼고 계산
                if(workHour > 8) { // 8시간을 초과할경우 초과로 처리
                    overallHours += (workHour - 8);
                }
            }
        }
        return new WeeklySumDto(totalHours, overallHours);
    }
}
