package com.example.exodia.attendance.repository;

import com.example.exodia.attendance.domain.Attendance;
import com.example.exodia.department.domain.Department;
import com.example.exodia.user.domain.User;
import io.lettuce.core.dynamic.annotation.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("SELECT a FROM Attendance a WHERE a.user = :user AND a.inTime >= :startOfWeek AND a.inTime < :endOfWeek")
    List<Attendance> findAllByUserAndWeek(@Param("user") User user, @Param("startOfWeek")LocalDateTime startOfWeek, @Param("endOfWeek")LocalDateTime endOfWeek);

    @Query("SELECT a FROM Attendance a WHERE a.user = :user AND a.inTime BETWEEN :startDate AND :endDate")
    List<Attendance> findAllByMemberAndInTimeBetween(@Param("user") User user,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);
    Optional<Attendance> findTopByUserAndOutTimeIsNull(User user);
    List<Attendance> findByUserIdAndInTimeBetween(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime);
    /*당일의 출근 조회*/
    Optional<Attendance> findByUserAndInTimeBetween(User user, LocalDateTime startOfDay, LocalDateTime endOfDay);

    // 사용자의 오늘 하루 기록
    @Query("SELECT a FROM Attendance a WHERE a.inTime >= :startOfDay AND a.inTime <= :currentTime")
    List<Attendance> findTodayRecords(LocalDateTime startOfDay, LocalDateTime currentTime);
//    Page<Attendance> findTodayRecords(LocalDateTime startOfDay, LocalDateTime currentTime, Pageable pageable);
}
