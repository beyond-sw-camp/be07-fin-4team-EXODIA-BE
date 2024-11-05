package com.example.exodia.attendance.service;

import com.example.exodia.attendance.domain.Attendance;
import com.example.exodia.attendance.domain.DayStatus;
import com.example.exodia.attendance.dto.*;
import com.example.exodia.attendance.repository.AttendanceRepository;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.department.domain.Department;
import com.example.exodia.department.repository.DepartmentRepository;
import com.example.exodia.user.domain.NowStatus;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.dto.UserStatusAndTime;
import com.example.exodia.user.repository.UserRepository;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AttendanceService {

	private final AttendanceRepository attendanceRepository;
	private final DepartmentRepository departmentRepository;
	private final UserRepository userRepository;

	@Autowired
	public AttendanceService(AttendanceRepository attendanceRepository, DepartmentRepository departmentRepository,
		UserRepository userRepository) {
		this.attendanceRepository = attendanceRepository;
		this.departmentRepository = departmentRepository;
		this.userRepository = userRepository;
	}

	/*출근시간 기록 용*/
	/* 유저 */
	@Transactional
	public Attendance workIn(AttendanceSaveDto dto) {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
		LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

		Optional<Attendance> existingAttendance = attendanceRepository.findByUserAndInTimeBetween(user, startOfToday,
			endOfToday);
		if (existingAttendance.isPresent()) {
			throw new RuntimeException("이미 오늘 출근 기록이 있습니다.");
		}

		user.setWorkIn();   //user.n_status 출근으로 기록
		Attendance attendance = dto.toEntity(user);
		attendance.setOutTime(null);  // 출근 시에는 퇴근 시간을 null로 설정
		return attendanceRepository.save(attendance);
	}

	/*퇴근시간 기록 용*/
	/* 유저 */
	@Transactional
	public Attendance workOut(AttendanceUpdateDto dto) {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		Attendance attendance = attendanceRepository.findTopByUserAndOutTimeIsNull(user)
			.orElseThrow(() -> new RuntimeException("출근 기록이 존재하지 않습니다"));

		user.setWorkOut();
		dto.updateEntity(attendance);  // 퇴근 시간 업데이트
		return attendanceRepository.save(attendance);
	}

	// 주어진 기간의 주차별 근무 시간 합산 정보 조회
	/* 유저 */
	@Transactional
	public List<WeeklySumDto> getWeeklySummaries(LocalDate startDate, LocalDate endDate) {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		// 출퇴근 시간 데이터 가져오기
		List<Attendance> attendances = attendanceRepository.findAllByMemberAndInTimeBetween(user,
			startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
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

	// @Transactional
	// public Map<String, List<AttendanceDetailDto>> getWeeklyDetails(LocalDate startDate, LocalDate endDate) {
	//     String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
	//
	//     User user = userRepository.findByUserNum(userNum).orElseThrow(()
	//             -> new RuntimeException("존재하지 않는 사원입니다"));
	//
	//     // 주어진 기간 내의 출퇴근 시간 데이터 가져오기
	//     List<Attendance> attendances = attendanceRepository.findAllByMemberAndInTimeBetween(user, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
	//     Map<String, List<AttendanceDetailDto>> weeklyDetails = new HashMap<>();
	//
	//     for (Attendance attendance : attendances) {
	//         LocalDate attendanceDate = attendance.getInTime().toLocalDate();
	//         String dayOfWeek = attendanceDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN); // 월, 화, 수, 목, 금
	//
	//         double workHours = calculateWorkHours(attendance);
	//         double overtimeHours = calculateOvertimeHours(attendance); // 초과 근무 시간 계산
	//
	//         // 요일별로 출퇴근 시간을 Dto로 만들어서 저장
	//         AttendanceDetailDto dto = new AttendanceDetailDto(
	//                 attendance.getInTime(),
	//                 attendance.getOutTime(),
	//                 workHours,
	//                 overtimeHours
	//         );
	//
	//         weeklyDetails.putIfAbsent(dayOfWeek, new ArrayList<>());
	//         weeklyDetails.get(dayOfWeek).add(dto);
	//     }
	//
	//     return weeklyDetails;
	// }

	// 근무 시간 계산 (출근 시간과 퇴근 시간 차이)
	private double calculateWorkHours(Attendance attendance) {
		LocalDateTime outTime = attendance.getOutTime() != null ? attendance.getOutTime() : LocalDateTime.now();
		return Duration.between(attendance.getInTime(), outTime).toHours();
	}

	public List<WeeklyAttendanceDto> getWeeklyAttendance(int year) {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		Long userId = user.getId();

		List<WeeklyAttendanceDto> weeklyAttendanceList = new ArrayList<>();
		LocalDate firstMondayOfYear = LocalDate.of(year, 1, 4).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

		for (int i = 0; i < 52; i++) {
			LocalDate startOfWeek = firstMondayOfYear.plusWeeks(i);
			LocalDate endOfWeek = startOfWeek.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

			List<Attendance> attendanceList = attendanceRepository.findByUserIdAndInTimeBetween(userId,
				startOfWeek.atStartOfDay(), endOfWeek.atTime(LocalTime.MAX));

			WeeklyAttendanceDto weeklyAttendance = new WeeklyAttendanceDto(i + 1, startOfWeek, endOfWeek,
				getDailyAttendance(attendanceList));
			weeklyAttendanceList.add(weeklyAttendance);
		}

		return weeklyAttendanceList;
	}

	private Map<String, DailyAttendanceDto> getDailyAttendance(List<Attendance> attendanceList) {
		Map<String, DailyAttendanceDto> dailyAttendance = new HashMap<>();
		for (Attendance attendance : attendanceList) {
			String dayOfWeek = attendance.getInTime().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
			dailyAttendance.put(dayOfWeek,
				new DailyAttendanceDto(attendance.getInTime(), attendance.getOutTime(), attendance.getHoursWorked()));
		}
		return dailyAttendance;
	}

	@Transactional
	public Map<String, List<AttendanceDetailDto>> getWeeklyDetailsWithOvertime(LocalDate startDate, LocalDate endDate) {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		// 출퇴근 시간 데이터 가져오기
		List<Attendance> attendances = attendanceRepository.findAllByMemberAndInTimeBetween(user,
			startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
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
		LocalTime outTime = attendance.getOutTime() != null ? attendance.getOutTime().toLocalTime() :
			LocalTime.now(); // 퇴근 시간이 없는 경우 현재 시간 사용

		double overtimeMinutes = 0;

		// 출근 시간이 09:00 이전인 경우
		if (inTime.isBefore(standardStartTime)) {
			overtimeMinutes += Duration.between(inTime, standardStartTime).toMinutes();
		}
		// 퇴근 시간이 18:00 이후인 경우
		if (outTime.isAfter(standardEndTime)) {
			overtimeMinutes += Duration.between(standardEndTime, outTime).toMinutes();
		}
		return overtimeMinutes / 60.0; // 시간 계산
	}

	/* 당일 출 퇴근 조회 */
	public DailyAttendanceDto getTodayAttendance() {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

		// 오늘의 날짜 범위를 계산 (00:00:00 ~ 23:59:59)
		LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
		LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

		Attendance attendance = attendanceRepository.findByUserAndInTimeBetween(
				user, startOfToday, endOfToday)
			.orElseThrow(() -> new RuntimeException("오늘 출퇴근 기록이 존재하지 않습니다."));
		return DailyAttendanceDto.fromEntity(attendance);
	}

	@Transactional  // 일단 보류
	public Map<String, List<User>> getDepartmentUsersAttendanceStatus() {
		// 로그인한 유저의 userNum 가져오기
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

		// 로그인한 유저 정보 가져오기
		User loggedInUser = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new RuntimeException("로그인한 유저 정보를 찾을 수 없습니다."));

		// 로그인한 유저의 부서 ID로 같은 부서에 속한 모든 유저 조회
		Long departmentId = loggedInUser.getDepartment().getId();
		List<User> departmentUsers = userRepository.findAllByDepartmentIdAndDelYn(departmentId, DelYN.N);

		// 오늘의 날짜 범위
		LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
		LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);

		// 출근한 사람들과 출근하지 않은 사람들을 담을 리스트
		List<User> presentUsers = new ArrayList<>();
		List<User> absentUsers = new ArrayList<>();

		// 같은 부서의 모든 유저에 대해 출근 여부 확인
		for (User user : departmentUsers) {
			// 유저의 오늘 출근 기록 조회
			Optional<Attendance> attendanceOptional = attendanceRepository.findByUserAndInTimeBetween(user,
				startOfToday, endOfToday);

			// if (attendanceOptional.isPresent() && attendanceOptional.get().getDayStatus() == DayStatus.O) {
			if (attendanceOptional.isPresent() && attendanceOptional.get().getNowStatus() == NowStatus.출근) {
				presentUsers.add(user); // 출근한 사람으로 분류
			} else {
				absentUsers.add(user); // 출근하지 않은 사람으로 분류
			}
		}
		// 출근한 사람들과 출근하지 않은 사람들 리스트를 결과로 반환
		Map<String, List<User>> attendanceStatusMap = new HashMap<>();
		attendanceStatusMap.put("출근한 사람들", presentUsers);
		attendanceStatusMap.put("출근하지 않은 사람들", absentUsers);

		return attendanceStatusMap;
	}

	private void collectAllChildrenById(Long departmentId, List<Department> allChildren, Set<Long> visited) {
		if (visited.contains(departmentId))
			return;
		visited.add(departmentId);

		Department department = departmentRepository.findById(departmentId)
			.orElseThrow(() -> new RuntimeException("부서 정보가 존재하지 않습니다."));
		allChildren.add(department);

		for (Department child : department.getChildren()) {
			collectAllChildrenById(child.getId(), allChildren, visited);
		}
	}

	public List<Department> getAllNestedChildrenById(Long departmentId) {
		List<Department> allChildren = new ArrayList<>();
		collectAllChildrenById(departmentId, allChildren, new HashSet<>());
		return allChildren;
	}

	@Transactional
	public Page<UserStatusAndTime> getTodayRecords(Pageable pageable) throws IOException {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		// 로그인한 유저 정보 가져오기
		User loggedInUser = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new IOException("로그인한 유저 정보를 찾을 수 없습니다."));

		Long departmentId = (Long)userRepository.findDepartmentIdByUserNum(userNum)
			.orElseThrow(() -> new IOException("로그인한 유저 정보를 찾을 수 없습니다."));
		List<Department> departments = getAllNestedChildrenById(departmentId);

		List<User> users = new ArrayList<>();
		for (Department department : departments) {
			List<User> departmentUsers = userRepository.findAllByDepartmentIdAndDelYn(department.getId(),
				DelYN.N); //같은 부서 사람들
			for (User user : departmentUsers) {
				users.add(user);
			}
		}
		LocalDateTime startOfDay = LocalDateTime.of(LocalDateTime.now().toLocalDate(), LocalTime.MIDNIGHT);
		LocalDateTime currentTime = LocalDateTime.now();

		List<UserStatusAndTime> times = new ArrayList<>();
		Page<Attendance> attendanceRecords = attendanceRepository.findTodayRecords(startOfDay, currentTime, pageable);

		for (User user : users) {
			// User와 매칭되는 Attendance를 찾음
			Attendance attendanceRecord = attendanceRecords.stream()
				.filter(att -> att.getUser().getId().equals(user.getId()))
				.findFirst()
				.orElse(null);

			if (attendanceRecord != null) {
				times.add(UserStatusAndTime.builder()
					.name(user.getName())
					.userNum(user.getUserNum())
					.departmentName(user.getDepartment().getName())
					.positionName(user.getPosition().getName())
					.profileImage(user.getProfileImage())
					.nowStatus(user.getN_status())
					.inTime(attendanceRecord.getInTime())
					.outTime(attendanceRecord.getOutTime())
					.build()
				);
			} else {
				// 매칭되는 Attendance가 없는 경우
				times.add(UserStatusAndTime.builder()
					.name(user.getName())
					.userNum(user.getUserNum())
					.departmentName(user.getDepartment().getName())
					.positionName(user.getPosition().getName())
					.profileImage(user.getProfileImage())
					.nowStatus(NowStatus.근무전)
					.inTime(null)
					.outTime(null)
					.build()
				);
			}
		}
		return new PageImpl<>(times, pageable, attendanceRecords.getTotalElements());
	}

	@Transactional
	public Attendance inMeetingStatus() throws IOException {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
		LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

		Attendance attendance = attendanceRepository.findByUserAndInTimeBetween(user, startOfToday, endOfToday)
			.orElseThrow(() -> new IOException("근태 기록이 존재하지 않습니다."));

		user.setMeetingStatus();   //user.n_status 회의중으로 기록
		attendance.setMeetingStatus();
		return attendance;
	}

	@Transactional
	public Attendance outMeetingStatus() throws IOException {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum).orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
		LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

		Attendance attendance = attendanceRepository.findByUserAndInTimeBetween(user, startOfToday, endOfToday)
			.orElseThrow(() -> new IOException("근태 기록이 존재하지 않습니다."));

		user.setWorkIn();   //user.n_status 출근으로 기록
		attendance.setWorkIn();
		return attendance;
	}
}
