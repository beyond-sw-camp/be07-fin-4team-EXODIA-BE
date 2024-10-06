package com.example.exodia.calendar.repository;

import com.example.exodia.calendar.domain.Calendar;

import com.example.exodia.department.domain.Department;
import com.example.exodia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    List<Calendar> findByUserAndDelYn(User user, String delYn);

    List<Calendar> findByDepartmentAndDelYn(Department department, String delYn);
    List<Calendar> findByTypeAndDelYn(String type, String delYn);
    List<Calendar> findByDelYn(String delYn);
}