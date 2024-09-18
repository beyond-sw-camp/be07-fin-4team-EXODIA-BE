package com.example.exodia.user.dto;

import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    private String id;
    private String departmentName;
    private String name;
    private String positionName;
    private LocalDate joinDate;

    public static UserInfoDto fromEntity(User user) {
        return new UserInfoDto(
                user.getUserId(),
                user.getDepartment().getName(),
                user.getName(),
                user.getPosition().getName(),
                user.getCreatedAt().toLocalDate()
        );
    }
}
