package com.example.exodia.course.repository;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.course.domain.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByDelYn(DelYN delYn, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.startTime = :startTime AND c.delYn = :delYn")
    List<Course> findCoursesStartingAt(@Param("startTime") LocalDateTime startTime, @Param("delYn") DelYN delYn);
}
