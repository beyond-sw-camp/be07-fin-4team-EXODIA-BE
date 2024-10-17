package com.example.exodia.calendar.service;


import com.example.exodia.calendar.domain.Calendar;
import com.example.exodia.calendar.dto.CalendarResponseDto;
import com.example.exodia.calendar.dto.CalendarSaveDto;
import com.example.exodia.calendar.dto.CalendarUpdateDto;
import com.example.exodia.calendar.repository.CalendarRepository;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.UserService;
import com.google.api.services.calendar.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    @Autowired
    private final CalendarRepository calendarRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final GoogleCalendarService googleCalendarService;
    @Autowired
    private final UserService userService;

    public CalendarService(CalendarRepository calendarRepository, UserRepository userRepository,
                           GoogleCalendarService googleCalendarService, UserService userService) {
        this.calendarRepository = calendarRepository;
        this.userRepository = userRepository;
        this.googleCalendarService = googleCalendarService;
        this.userService = userService;
    }

    /* 인증된 사용자 정보 */
    private User getAuthenticatedUser() {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 존재하지 않습니다."));
    }

    /* 캘린더 이벤트 생성 */
    @Transactional
    public CalendarResponseDto createCalendarEvent(CalendarSaveDto dto) throws Exception {
        User user = getAuthenticatedUser();

        // 회사일정 타입일 경우 관리자 권한 체크
        if ("회사일정".equals(dto.getType())) {
            userService.checkHrAuthority(user.getDepartment().getId().toString());
        }

        Calendar calendar = Calendar.fromDto(dto, user);
        calendarRepository.save(calendar);

        // Google Calendar API 연동 로직 추가
        Event googleEvent = googleCalendarService.addEventToGoogleCalendar(dto);
        calendar.setGoogleEventId(googleEvent.getId());

        return CalendarResponseDto.fromEntity(calendar);
    }

    /* 캘린더 이벤트 업데이트 */
    @Transactional
    public CalendarResponseDto updateCalendarEvent(Long calendarId, CalendarUpdateDto dto) throws Exception {
        User user = getAuthenticatedUser();
        Calendar calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new IllegalArgumentException("해당 이벤트가 없습니다."));

        if (!calendar.getUser().getId().equals(user.getId())) {
            throw new IllegalAccessException("수정 권한이 없습니다.");
        }
        // 로컬 데이터베이스 업데이트
        calendar.updateFromDto(dto);
        calendarRepository.save(calendar);

        // Google Calendar API 연동 로직
        googleCalendarService.updateEventInGoogleCalendar(calendar.getGoogleEventId(), dto);

        return CalendarResponseDto.fromEntity(calendar);
    }

    /* 캘린더 이벤트 삭제 */
    @Transactional
    public void deleteCalendarEvent(Long calendarId) throws Exception {
        User user = getAuthenticatedUser();
        Calendar calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new IllegalArgumentException("해당 이벤트가 없습니다."));

        if (!calendar.getUser().getId().equals(user.getId())) {
            throw new IllegalAccessException("삭제 권한이 없습니다.");
        }

        // 로컬 데이터베이스에서 이벤트 삭제
        calendarRepository.delete(calendar);

        // Google Calendar API 연동 로직
        googleCalendarService.deleteEventInGoogleCalendar(calendar.getGoogleEventId());
    }

    /* 공휴일을 저장하는 로직 */
    @Transactional
    public  List<CalendarResponseDto> saveHolidaysToDatabase() throws GeneralSecurityException, IOException {
        // Google Calendar API에서 한국 공휴일 목록 가져오기
        List<Event> holidayEvents = googleCalendarService.getHolidayEvents(
                "ko.south_korea#holiday@group.v.calendar.google.com",
                LocalDateTime.now().withDayOfYear(1),
                LocalDateTime.now().withDayOfYear(365)
        );
        List<CalendarResponseDto> savedHolidays = new ArrayList<>();
        // 공휴일 데이터를 Calendar 엔티티로 변환하여 저장
        for (Event event : holidayEvents) {
            LocalDate startDate = event.getStart().getDate() != null ? LocalDate.parse(event.getStart().getDate().toString()) : null;
            LocalDate endDate = event.getEnd().getDate() != null ? LocalDate.parse(event.getEnd().getDate().toString()) : null;
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime endDateTime = endDate != null ? endDate.atStartOfDay() : null;

            // 이미 저장된 공휴일인지 확인 (중복 저장 방지)
            if (!calendarRepository.existsByTitleAndStartTime(event.getSummary(), startDateTime)) {
                Calendar holiday = new Calendar();
                holiday.setTitle(event.getSummary());
                holiday.setType("공휴일");
                holiday.setStartTime(startDateTime);
                holiday.setEndTime(endDateTime);
                holiday.setGoogleEventId(event.getId());
                holiday.setDelYn("N");
                holiday.setUser(null);

                calendarRepository.save(holiday);
                savedHolidays.add(CalendarResponseDto.fromEntity(holiday));
            }
        }
        return savedHolidays;
    }

    /* 부서별 캘린더 조회 */
    @Transactional(readOnly = true)
    public List<CalendarResponseDto> getDepartmentEvents() {
        User user = getAuthenticatedUser();
        List<Calendar> departmentEvents = calendarRepository.findByDepartmentAndDelYn(user.getDepartment(), "N").stream()
                .filter(calendar -> "부서".equals(calendar.getType()))
                .collect(Collectors.toList());

        return departmentEvents.stream()
                .map(CalendarResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    /* 유저 정보 조회 */
    @Transactional(readOnly = true)
    public List<CalendarResponseDto> getPersonalEventsForAuthenticatedUser() {
        User user = getAuthenticatedUser(); // 현재 로그인한 사용자 정보 가져오기

        // 현재 사용자 본인이 생성한 유저 타입의 이벤트만 조회
        List<Calendar> personalEvents = calendarRepository.findByUserAndDelYn(user, "N").stream()
                .filter(calendar -> "유저".equals(calendar.getType()))  // "유저" 타입 이벤트만 필터링
                .collect(Collectors.toList());

        return personalEvents.stream()
                .map(CalendarResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    /* 회사일정 조회 */
    @Transactional(readOnly = true)
    public List<CalendarResponseDto> getCompanyEvents() {
        List<Calendar> companyEvents = calendarRepository.findByTypeAndDelYn("회사일정", "N");

        return companyEvents.stream()
                .map(CalendarResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /* 사용자, 부서, 회사, 공휴일 일정 조회 */
    @Transactional(readOnly = true)
    public List<CalendarResponseDto> getUserAndHolidayCalendars() throws GeneralSecurityException, IOException {
        User user = getAuthenticatedUser();

//        List<CalendarResponseDto> userCalendars = getUserCalendarsForAuthenticatedUser();

        // 유저 필터링
        List<CalendarResponseDto> userOneCalendars = getPersonalEventsForAuthenticatedUser();
        // 부서필터링 조회
        List<CalendarResponseDto> departmentCalendars = getDepartmentEvents();
        // 전체필터링
        List<CalendarResponseDto> companyCalendars = getCompanyEvents();


        // Google Calendar API에서 한국 공휴일 목록 가져오기
        List<Event> holidayEvents = googleCalendarService.getHolidayEvents(
                "ko.south_korea#holiday@group.v.calendar.google.com",
                LocalDateTime.now().withDayOfYear(1), // 연도의 첫 날
                LocalDateTime.now().withDayOfYear(365) // 연도의 마지막 날
        );
        List<CalendarResponseDto> holidayDtos = holidayEvents.stream()
                .map(event -> {
                    LocalDate startDate = event.getStart().getDate() != null ? LocalDate.parse(event.getStart().getDate().toString()) : null;
                    LocalDate endDate = event.getEnd().getDate() != null ? LocalDate.parse(event.getEnd().getDate().toString()) : null;
                    LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
                    LocalDateTime endDateTime = endDate != null ? endDate.atStartOfDay() : null;

                    return new CalendarResponseDto(
                            null,
                            event.getSummary(),
                            "공휴일",
                            startDateTime,
                            endDateTime,
                            "HOLIDAY",
                            "공휴일",
                            null
                    );
                })
                .collect(Collectors.toList());


        // 전체 이벤트 리스트 생성
        List<CalendarResponseDto> allEvents = new ArrayList<>();
        allEvents.addAll(companyCalendars);
        allEvents.addAll(departmentCalendars); // 부서 이벤트
        allEvents.addAll(holidayDtos); // 공휴일
        allEvents.addAll(userOneCalendars);

        return allEvents;
    }




    public CalendarResponseDto findByTitle(String title) {
        Calendar calendar = calendarRepository.findByTitle(title);
        if (calendar != null) {
            return CalendarResponseDto.fromEntity(calendar);
        }
        return null;
    }

    /* 사용자 별 캘린더 조회 */
//    @Transactional(readOnly = true)
//    public List<CalendarResponseDto> getUserCalendarsForAuthenticatedUser() {
//        User user = getAuthenticatedUser();
//        List<Calendar> calendars = calendarRepository.findByUserAndDelYn(user, "N");
//
//        return calendars.stream()
//                .filter(calendar -> {
//                    switch (calendar.getType()) {
//                        case "유저":
//                            // "유저" 타입: 본인이 작성한 이벤트만 볼 수 있음
//                            return calendar.getUser().getId().equals(user.getId());
//                        case "부서":
//                            // "부서" 타입: 같은 부서의 사람들만 볼 수 있음
//                            return calendar.getDepartment().getId().equals(user.getDepartment().getId());
//                        case "회사일정":
//                            // "회사일정" 타입: 모든 사용자가 볼 수 있음
//                            return true;
//                        default:
//                            return false;
//                    }
//                })
//                .map(CalendarResponseDto::fromEntity)
//                .collect(Collectors.toList());
//    }
    /* 유저 및 부서 일정 조회 */
//    @Transactional(readOnly = true)
//    public List<CalendarResponseDto> getUserAndDepartmentEvents() {
//        User user = getAuthenticatedUser();
//
//        // 본인이 작성한 유저 타입의 이벤트 조회
//        List<CalendarResponseDto> userEvents = getUserCalendarsForAuthenticatedUser();
//
//        // 부서 이벤트 조회 (같은 부서의 사람들만 볼 수 있음)
//        List<CalendarResponseDto> departmentEvents = getDepartmentEvents();
//
//        // 모든 유저가 볼 수 있는 회사일정 이벤트 조회
//        List<CalendarResponseDto> companyEvents = getCompanyEvents();
//
//        // 전체 이벤트 리스트 반환
//        List<CalendarResponseDto> allEvents = new ArrayList<>();
//        allEvents.addAll(userEvents);
//        allEvents.addAll(departmentEvents);
//        allEvents.addAll(companyEvents);
//
//        return allEvents;
//    }
}


