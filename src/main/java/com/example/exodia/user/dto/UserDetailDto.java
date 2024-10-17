package com.example.exodia.user.dto;

import com.example.exodia.user.domain.Gender;
import com.example.exodia.user.domain.HireType;
import com.example.exodia.user.domain.Status;
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
    private String address;
    private String password;
    private String socialNum;
    private Gender gender;
    private Status status;

    public static UserDetailDto fromEntity(User user) {
        return new UserDetailDto(
                user.getUserNum(),
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getPosition() != null ? user.getPosition().getId() : null,
                UserDto.parseBirthDate(user.getSocialNum()),
                user.getPhone(),
                user.getCreatedAt().toLocalDate(),
                user.getHireType(),
                user.getAnnualLeave(),
                user.getEmail(),
                user.getName(),
                user.getProfileImage(),
                user.getAddress(),
                user.getPassword(),
                user.getSocialNum(),
                user.getGender(),
                user.getStatus()
        );
    }
}
