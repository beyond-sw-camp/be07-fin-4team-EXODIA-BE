package com.example.exodia.user.dto;

import com.example.exodia.user.domain.HireType;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private String userNum;
    private String departmentName;
    private String positionName;
    private String phone;
    private LocalDate joinDate;
    private String name;
    private String profileImage;
    private double annualLeave;

    public static UserProfileDto fromProfileEntity(User user) {
        return new UserProfileDto(
                user.getUserNum(),
                user.getDepartment().getName(),
                user.getPosition().getName(),
                user.getPhone(),
                user.getCreatedAt().toLocalDate(),
                user.getName(),
                user.getProfileImage(),
                user.getAnnualLeave()
        );
    }
}
