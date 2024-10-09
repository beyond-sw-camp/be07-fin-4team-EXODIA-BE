package com.example.exodia.calendar.domain;

import com.example.exodia.calendar.dto.CalendarSaveDto;
import com.example.exodia.calendar.dto.CalendarUpdateDto;
import com.example.exodia.department.domain.Department;
import com.example.exodia.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "del_yn = 'N'")
public class Calendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String content;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private String type; // "회사일정" , "부서" , "유저"

    @Column(name = "del_yn", nullable = false)
    private String delYn = "N";

    @Column(name = "google_event_id")
    private String googleEventId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    public static Calendar fromDto(CalendarSaveDto dto, User user) {
        return Calendar.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .startTime(LocalDateTime.parse(dto.getStartTime()))
                .endTime(LocalDateTime.parse(dto.getEndTime()))
                .type(dto.getType())
                .delYn("N")
                .user(user)
                .department(user.getDepartment())
                .build();
    }

    public void updateFromDto(CalendarUpdateDto dto) {
        this.title = dto.getTitle();
        this.content = dto.getContent();
        this.startTime = LocalDateTime.parse(dto.getStartTime());
        this.endTime = LocalDateTime.parse(dto.getEndTime());
        this.type = dto.getType();
    }
}