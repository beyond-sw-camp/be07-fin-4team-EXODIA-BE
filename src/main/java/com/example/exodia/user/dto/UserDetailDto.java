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
public class UserDetailDto {
    private String userNum;
    private Long departmentId;
    private Long positionId;
    private String birthDate;
    private String phone;
    private LocalDate joinDate;
    private HireType hireType;
    private int annualLeave;
    private String email;
    private String name;
    private String profileImage;

    public static UserDetailDto fromEntity(User user) {
        return new UserDetailDto(
                user.getUserNum(),
                user.getDepartment().getId(),
                user.getPosition().getId(),
                UserDto.parseBirthDate(user.getSocialNum()),
                user.getPhone(),
                user.getCreatedAt().toLocalDate(),
                user.getHireType(),
                user.getAnnualLeave(),
                user.getEmail(),
                user.getName(),
                user.getProfileImage()
        );
    }
}
