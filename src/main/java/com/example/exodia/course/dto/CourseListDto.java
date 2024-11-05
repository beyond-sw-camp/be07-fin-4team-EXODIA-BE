package com.example.exodia.course.dto;

import com.example.exodia.course.domain.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseListDto {

    private Long id;
    private String courseName;
    private String content;
    private String courseUrl;
    private int maxParticipants; // 최대 신청인원
    private int currentParticipants;
    private String UserDepartmentName; // 유저의 부서명 받을꺼
    private LocalDateTime startTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseListDto fromEntity(Course course, int currentParticipants) {
        return CourseListDto.builder()
                .id(course.getId())
                .courseName(course.getCourseName())
                .content(course.getContent())
                .courseUrl(course.getCourseUrl())
                .maxParticipants(course.getMaxParticipants())
                .currentParticipants(currentParticipants)
                .UserDepartmentName(course.getUser().getDepartment().getName())
                .startTime(course.getStartTime())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

}
