package com.example.exodia.registration.repository;

import com.example.exodia.course.domain.Course;
import com.example.exodia.registration.domain.Registration;
import com.example.exodia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    boolean existsByCourseAndUser(Course course, User user);
    List<Registration> findAllByCourseAndRegistrationStatus(Course course, String registrationStatus);
    int countByCourse(Course course);
    // 사원의 모든 강좌 조회
    List<Registration> findAllByUser(User user);
    // 강좌당 등록된 신청자 조회
    List<Registration> findAllByCourse(Course course);
}
