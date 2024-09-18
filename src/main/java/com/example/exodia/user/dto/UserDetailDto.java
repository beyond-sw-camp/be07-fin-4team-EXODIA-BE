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
    private String Id;
    private String departmentName;
    private String positionName;
    private String birthDate;
    private String phone;
    private LocalDate joinDate;
    private HireType hireType;
    private int annualLeave;

    public static UserDetailDto fromEntity(User user) {
        return new UserDetailDto(
                user.getUserId(),
                user.getDepartment().getName(),
                user.getPosition().getName(),
                UserDto.parseBirthDate(user.getSocialNum()),
                user.getPhone(),
                user.getCreatedAt().toLocalDate(),
                user.getHireType(),
                user.getAnnualLeave()
        );
    }
}
