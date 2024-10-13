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
}
