package com.example.exodia.registration.dto;

import com.example.exodia.course.domain.Course;
import com.example.exodia.registration.domain.Registration;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationCreateDto {
    private Long courseId;
    private String userNum;
    private String registrationStatus;

    public Registration toEntity(Course course, User user) {
        return Registration.builder()
                .course(course)
                .user(user)
                .registrationStatus(this.registrationStatus)
                .build();
    }

}
