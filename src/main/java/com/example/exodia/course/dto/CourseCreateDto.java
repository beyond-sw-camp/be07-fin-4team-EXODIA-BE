package com.example.exodia.course.dto;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.course.domain.Course;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCreateDto {

    private String courseName;
    private String content;
    private String courseUrl;
    private int maxParticipants;
    private LocalDateTime startTime;

    public Course toEntity(User user) {
        return Course.builder()
                .courseName(this.courseName)
                .content(this.content)
                .courseUrl(this.courseUrl)
                .maxParticipants(this.maxParticipants)
                .startTime(this.startTime)
                .user(user)
                .build();
    }
}
