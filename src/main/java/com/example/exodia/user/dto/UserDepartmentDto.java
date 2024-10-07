package com.example.exodia.user.dto;

import com.example.exodia.department.dto.DepartmentDto;
import com.example.exodia.user.domain.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDepartmentDto {
    private String userNum;
    private String name;
    private String email;
    private String phone;
    private DepartmentDto department;
    private String positionName;
    private String joinDate;
    private String birthDate;

    public static UserDepartmentDto fromEntity(User user) {
        UserDepartmentDto dto = new UserDepartmentDto();
        dto.setUserNum(user.getUserNum());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setDepartment(DepartmentDto.fromEntity(user.getDepartment()));
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
