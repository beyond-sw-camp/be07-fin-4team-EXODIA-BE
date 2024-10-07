package com.example.exodia.attendance.service;

import com.example.exodia.attendance.domain.Attendance;
import com.example.exodia.attendance.dto.*;
import com.example.exodia.attendance.repository.AttendanceRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

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
    /* 유저 */
    @Transactional
    public Attendance workIn(AttendanceSaveDto dto) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

        Attendance attendance = dto.toEntity(user);
        return attendanceRepository.save(attendance);
    }

    /*퇴근시간 기록 용*/
    /* 유저 */
    @Transactional
    public Attendance workOut(AttendanceUpdateDto dto) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

        Attendance attendance = attendanceRepository.findTopByUserAndOutTimeIsNull(user).orElseThrow(() -> new RuntimeException("출근 기록이 존재하지 않습니다"));

        dto.updateEntity(attendance);
        return attendanceRepository.save(attendance);
    }

    // 주어진 기간의 주차별 근무 시간 합산 정보 조회
    /* 유저 */
    @Transactional
    public List<WeeklySumDto> getWeeklySummaries(LocalDate startDate, LocalDate endDate) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

        // 출퇴근 시간 데이터 가져오기
        List<Attendance> attendances = attendanceRepository.findAllByMemberAndInTimeBetween(user, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        // 주차별 근무 시간 합산
        Map<LocalDate, WeeklySumDto> weeklySummaryMap = new HashMap<>();

        for (Attendance attendance : attendances) {
            // 해당 출근 시간이 속한 주차(목요일 기준) 계산 * 국제 표준 ISO-8601 기준으로 계산
            LocalDate weekOfYear = WeekUtils.getWeekOfYear(attendance.getInTime().toLocalDate());
            // 해당 주차가 주차 맵에 없으면 새로 생성
            weeklySummaryMap.putIfAbsent(weekOfYear, new WeeklySumDto(0, 0, weekOfYear, weekOfYear.plusDays(6)));

            // 근무시간 + 초과시간 계산
            WeeklySumDto weeklySummary = weeklySummaryMap.get(weekOfYear); // 주차 근무 일수
            double hoursWorked = calculateWorkHours(attendance);
            weeklySummary.setTotalHours(weeklySummary.getTotalHours() + hoursWorked - 1); //점심시간 1시간 빼
            if (hoursWorked > 8) { // 현 계산식 : 일 8시시간 work 이후는 초과시간으로 계산
                weeklySummary.setOvertimeHours(weeklySummary.getOvertimeHours() + (hoursWorked - 8));
            }
        }

        return weeklySummaryMap.values().stream()
                .sorted(Comparator.comparing(WeeklySumDto::getStartOfWeek))
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, List<AttendanceDetailDto>> getWeeklyDetails(LocalDate startDate, LocalDate endDate) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUserNum(userNum).orElseThrow(()
                -> new RuntimeException("존재하지 않는 사원입니다"));

        // 주어진 기간 내의 출퇴근 시간 데이터 가져오기
        List<Attendance> attendances = attendanceRepository.findAllByMemberAndInTimeBetween(user, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        Map<String, List<AttendanceDetailDto>> weeklyDetails = new HashMap<>();

        for (Attendance attendance : attendances) {
            LocalDate attendanceDate = attendance.getInTime().toLocalDate();
            String dayOfWeek = attendanceDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN); // 월, 화, 수, 목, 금

            double workHours = calculateWorkHours(attendance);
            double overtimeHours = calculateOvertimeHours(attendance); // 초과 근무 시간 계산

            // 요일별로 출퇴근 시간을 Dto로 만들어서 저장
            AttendanceDetailDto dto = new AttendanceDetailDto(
                    attendance.getInTime(),
                    attendance.getOutTime(),
                    workHours,
                    overtimeHours
            );

            weeklyDetails.putIfAbsent(dayOfWeek, new ArrayList<>());
            weeklyDetails.get(dayOfWeek).add(dto);
        }

        return weeklyDetails;
    }

    // 근무 시간 계산 (출근 시간과 퇴근 시간 차이)
    private double calculateWorkHours(Attendance attendance) {
        if (attendance.getInTime() != null && attendance.getOutTime() != null) {
            return Duration.between(attendance.getInTime(), attendance.getOutTime()).toHours();
        }
        return 0;
    }

    //    public WeeklySumDto getWeeklySum() {
//        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
//        User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));
//
//        LocalDateTime startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
//        LocalDateTime endOfWeek = startOfWeek.plusDays(7);
//
//        List<Attendance> weeklyAttendance = attendanceRepository.findAllByUserAndWeek(user, startOfWeek, endOfWeek);
//        return calculateWeeklySum(weeklyAttendance);
//    }

//    public WeeklySumDto calculateWeeklySum(List<Attendance> weeklyAttendance) {
//        WeeklySumDto weeklySumDto = new WeeklySumDto();
//        double totalHours = 0;
//        double overallHours = 0;
//
//        for (Attendance attendance : weeklyAttendance) {
//            if (attendance.getInTime() != null && attendance.getOutTime() != null) {
//                double workHour = Duration.between(attendance.getInTime(), attendance.getOutTime()).toHours();
//                totalHours += workHour -1; // 점심시간 빼고 계산
//                if(workHour > 8) { // 8시간을 초과할경우 초과로 처리
//                    overallHours += (workHour - 8);
//                }
//            }
//        }
//        return new WeeklySumDto(totalHours, overallHours);
//    }

    public List<WeeklyAttendanceDto> getWeeklyAttendance(int year) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

        Long userId = user.getId();

        List<WeeklyAttendanceDto> weeklyAttendanceList = new ArrayList<>();
        LocalDate firstMondayOfYear = LocalDate.of(year, 1, 4).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (int i = 0; i < 52; i++) {
            LocalDate startOfWeek = firstMondayOfYear.plusWeeks(i);
            LocalDate endOfWeek = startOfWeek.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

            List<Attendance> attendanceList = attendanceRepository.findByUserIdAndInTimeBetween(userId, startOfWeek.atStartOfDay(), endOfWeek.atTime(LocalTime.MAX));

            WeeklyAttendanceDto weeklyAttendance = new WeeklyAttendanceDto(i + 1, startOfWeek, endOfWeek, getDailyAttendance(attendanceList));
            weeklyAttendanceList.add(weeklyAttendance);
        }

        return weeklyAttendanceList;
    }

    private Map<String, DailyAttendanceDto> getDailyAttendance(List<Attendance> attendanceList) {
        Map<String, DailyAttendanceDto> dailyAttendance = new HashMap<>();
        for (Attendance attendance : attendanceList) {
            String dayOfWeek = attendance.getInTime().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
            dailyAttendance.put(dayOfWeek, new DailyAttendanceDto(attendance.getInTime(), attendance.getOutTime(), attendance.getHoursWorked()));
        }
        return dailyAttendance;
    }

    @Transactional
    public Map<String, List<AttendanceDetailDto>> getWeeklyDetailsWithOvertime(LocalDate startDate, LocalDate endDate) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

        // 출퇴근 시간 데이터 가져오기
        List<Attendance> attendances = attendanceRepository.findAllByMemberAndInTimeBetween(user, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        Map<String, List<AttendanceDetailDto>> weeklyDetails = new HashMap<>();

        for (Attendance attendance : attendances) {
            LocalDate attendanceDate = attendance.getInTime().toLocalDate();
            String dayOfWeek = attendanceDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);

            // 근무 시간 계산
            double workHours = calculateWorkHours(attendance);
            double overtimeHours = calculateOvertimeHours(attendance); // 초과 근무 시간 계산

            AttendanceDetailDto detail = new AttendanceDetailDto(
                    attendance.getInTime(),
                    attendance.getOutTime(),
                    workHours,
                    overtimeHours
            );

            weeklyDetails.putIfAbsent(dayOfWeek, new ArrayList<>());
            weeklyDetails.get(dayOfWeek).add(detail);
        }

        return weeklyDetails;
    }
    private double calculateOvertimeHours(Attendance attendance) {
        LocalTime standardStartTime = LocalTime.of(9, 0);
        LocalTime standardEndTime = LocalTime.of(18, 0);

        LocalTime inTime = attendance.getInTime().toLocalTime();
        LocalTime outTime = attendance.getOutTime().toLocalTime();

        double overtimeMinutes = 0;

        // 출근 시간이 09:00 이전인 경우
        if (inTime.isBefore(standardStartTime)) {
            overtimeMinutes += Duration.between(inTime, standardStartTime).toMinutes();
        }

        // 퇴근 시간이 18:00 이후인 경우
        if (outTime.isAfter(standardEndTime)) {
            overtimeMinutes += Duration.between(standardEndTime, outTime).toMinutes();
        }

        return overtimeMinutes / 60.0; // 시간을 반환
    }
}
