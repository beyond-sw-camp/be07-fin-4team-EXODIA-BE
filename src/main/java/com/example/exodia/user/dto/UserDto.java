package com.example.exodia.user.dto;

import com.example.exodia.user.domain.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {
    private String userNum;
    private String name;
    private String email;
    private String phone;
    private String departmentName;
    private String positionName;
    private String joinDate;
    private String birthDate;

    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setUserNum(user.getUserNum());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setDepartmentName(user.getDepartment().getName());
        dto.setPositionName(user.getPosition().getName());
        dto.setJoinDate(user.getCreatedAt().toString());
        dto.setBirthDate(parseBirthDate(user.getSocialNum()));
        return dto;
    }

    public static String parseBirthDate(String socialNum) {
        String prefix = "19";
        int year = Integer.parseInt(socialNum.substring(0, 2));

        if (year <= 99 && year >= 00) {
            prefix = "20";
        }
        return prefix + socialNum.substring(0, 6); // 예: 20001212
    }
}
