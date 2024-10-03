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

    public CalendarService(CalendarRepository calendarRepository, UserRepository userRepository, GoogleCalendarService googleCalendarService, UserService userService) {
        this.calendarRepository = calendarRepository;
        this.userRepository = userRepository;
        this.googleCalendarService = googleCalendarService;
        this.userService = userService;
    }
    /* 현재 인증된 사용자 정보 */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("인증된 사용자가 없습니다.");
        }
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userRepository.findByUserNum(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 존재하지 않습니다."));
        } else {
            throw new IllegalArgumentException("UserDetails 타입이 아닙니다.");
        }
    }

    /* 이벤트 생성 */
    @Transactional
    public CalendarResponseDto createCalendarEvent(CalendarSaveDto dto) throws Exception {
        User user = getAuthenticatedUser();

        // "회사일정" 타입일 경우 관리자 권한 체크
        if ("회사".equals(dto.getType())) {
            userService.checkHrAuthority(user.getDepartment().getId().toString());
        }

        Calendar calendar = Calendar.fromDto(dto, user);

        if ("부서".equals(dto.getType())) {
            List<User> departmentUsers = userRepository.findByDepartmentAndDelYn(user.getDepartment(), DelYN.N);
            calendarRepository.save(calendar);
        }
        // "유저" 또는 "회사" 타입일 경우 일반적으로 저장
        else {
            calendarRepository.save(calendar);
        }

        // Google Calendar API 연동 로직 추가
        Event googleEvent = googleCalendarService.addEventToGoogleCalendar(dto);
        calendar.setGoogleEventId(googleEvent.getId());

        return CalendarResponseDto.fromEntity(calendar);
    }




    /* 이벤트 업데이트 */
    @Transactional
    public CalendarResponseDto updateCalendarEvent(Long calendarId, CalendarUpdateDto dto) throws Exception {
        User user = getAuthenticatedUser();//
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
    /* 이벤트 삭제 */
    @Transactional
    public void deleteCalendarEvent(Long calendarId) throws Exception {
        User user = getAuthenticatedUser();//
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

    @Transactional(readOnly = true)
    public List<CalendarResponseDto> getUserAndHolidayCalendars() throws GeneralSecurityException, IOException {
        User user = getAuthenticatedUser();

        List<CalendarResponseDto> userCalendars = getUserCalendars(user.getId());

        // 부서 이벤트 조회
        List<CalendarResponseDto> departmentCalendars = getDepartmentEvents(user);

        // googleCalendarService에서 한국 공휴일 목록
        List<Event> holidayEvents = googleCalendarService.getHolidayEvents(
                "ko.south_korea#holiday@group.v.calendar.google.com",
                LocalDateTime.now().withDayOfYear(1), // 현 연도의 첫
                LocalDateTime.now().withDayOfYear(365) // 현 연도의 막
        );

        List<CalendarResponseDto> holidayDtos = holidayEvents.stream()
                .map(event -> {
                    // localdateTime으로 이벤트를 저장할 수 없기 때문에 localDate 으로 파싱
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
        // user가 생성한 events 을 불러와서 allEvents 저장
        List<CalendarResponseDto> allEvents = new ArrayList<>(userCalendars);
        allEvents.addAll(departmentCalendars); // 부서 이벤트
        allEvents.addAll(holidayDtos); // 공휴일
        return allEvents;
    }

    @Transactional(readOnly = true)
    public List<CalendarResponseDto> getUserCalendars(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("해당 사원은 없는 사원입니다"));
        List<Calendar> calendars = calendarRepository.findByUserAndDelYn(user, "N");

        return calendars.stream()
                .map(CalendarResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CalendarResponseDto> getDepartmentEvents(User user) {
        List<Calendar> departmentEvents = calendarRepository.findByDepartmentAndDelYn(user.getDepartment(), "N");

        return departmentEvents.stream()
                .map(CalendarResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

}