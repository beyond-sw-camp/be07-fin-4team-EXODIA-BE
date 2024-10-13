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
    private String courseUrl;
    private int maxParticipants; // 최대 신청인원
    private int remainingParticipants; // 남은 신청인원 ( 최대 - 신청 )
    private String UserDepartmentName; // 유저의 부서명 받을꺼
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseListDto fromEntity(Course course, int currentParticipants) {
        return CourseListDto.builder()
                .id(course.getId())
                .courseName(course.getCourseName())
                .courseUrl(course.getCourseUrl())
                .maxParticipants(course.getMaxParticipants())
                .remainingParticipants(course.getMaxParticipants() - currentParticipants)
                .UserDepartmentName(course.getUser().getDepartment().getName())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

}
