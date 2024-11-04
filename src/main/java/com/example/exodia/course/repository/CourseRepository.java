package com.example.exodia.course.repository;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.course.domain.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByDelYn(DelYN delYn, Pageable pageable);
}
