package com.example.exodia.calendar.domain;

import com.example.exodia.calendar.service.CalendarService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//@Component
//public class HolidayScheduler {
//
//    private final CalendarService calendarService;
//
//    public HolidayScheduler(CalendarService calendarService) {
//        this.calendarService = calendarService;
//    }
//
//    // 매년 1월 1일에 공휴일 정보를 저장
//    @Scheduled(cron = "0 0 0 1 1 ?")
//    public void saveHolidays() {
//        try {
//            calendarService.saveHolidaysToDatabase();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
