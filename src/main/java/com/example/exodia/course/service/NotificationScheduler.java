//package com.example.exodia.course.service;
//
//import com.example.exodia.course.domain.Course;
//import com.example.exodia.course.repository.CourseRepository;
//import com.example.exodia.notification.service.NotificationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//public class NotificationScheduler {
//
//    @Autowired
//    private CourseRepository courseRepository;
//
//    @Autowired
//    private NotificationService notificationService;
//
//    @Scheduled(cron = "0 */5 * * * *") // 매 5분마다 실행
//    public void sendCourseReminder() {
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime reminderTime = now.plusMinutes(3); // 현재 시간으로부터 3분 후
//
//        // 3분 후 시작되는 코스를 조회
//        List<Course> upcomingCourses = courseRepository.findCoursesStartingAt(reminderTime);
//        for (Course course : upcomingCourses) {
//            String message = "[Reminder] 코스 " + course.getCourseName() + "이(가) 3분 후 시작합니다!";
//            notificationService.sendNotification(course.getUser(), message); // 알림 전송
//        }
//    }
//}
