package com.example.exodia.course.dto;

import com.example.exodia.course.domain.Course;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseUpdateDto {

    private String courseName;
    private String content;
    private String courseUrl;
    private int maxParticipants;

    public Course toEntity(Course existingCourse, User user) {
        return Course.builder()
                .id(existingCourse.getId())
                .courseName(this.courseName)
                .content(this.content)
                .courseUrl(this.courseUrl)
                .maxParticipants(this.maxParticipants)
                .user(user)
                .build();
    }
}
