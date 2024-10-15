package com.example.exodia.user.dto;

import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAttendanceDto {
    private String userNum;
    private Long departmentId;
    private Long positionId;
    private String name;
    private String profileImage;

    public static UserAttendanceDto fromEntity(User user) {
        return new UserAttendanceDto(
                user.getUserNum(),
                user.getDepartment().getId(),
                user.getPosition().getId(),
                user.getName(),
                user.getProfileImage()
        );
    }

}
