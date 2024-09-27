package com.example.exodia.attendance.domain;

import com.example.exodia.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
//@Where(clause = "del_yn = 'N'")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime inTime; // 출근 시간

    private LocalDateTime outTime; // 퇴근 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayStatus dayStatus;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
