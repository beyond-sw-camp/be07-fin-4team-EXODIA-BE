package com.example.exodia.calendar.repository;

import com.example.exodia.calendar.domain.Calendar;

import com.example.exodia.department.domain.Department;
import com.example.exodia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    /* 유저 필터링 */
    List<Calendar> findByUserAndDelYn(User user, String delYn);
    /* 부서 필터링 */
    List<Calendar> findByDepartmentAndDelYn(Department department, String delYn);
    /* 타입 필터링 */
    List<Calendar> findByTypeAndDelYn(String type, String delYn);

    /* 휴일 필터링 */
    boolean existsByTitleAndStartTime(String title, LocalDateTime startTime);
}