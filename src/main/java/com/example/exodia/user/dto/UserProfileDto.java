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
    private String userNum; //사번
    private String departmentName; // 부서
    private String positionName; // 직책
//    private String birthDate; // 생일
    private String phone; // 폰번
    private LocalDate joinDate; // 입사일
    private String name; // 이름
    private String profileImage; // 프로필 이미지
    private int annualLeave; // 휴가 정보

    public static UserProfileDto fromProfileEntity(User user) {
        return new UserProfileDto(
                user.getUserNum(),
                user.getDepartment().getName(),
                user.getPosition().getName(),
//                UserDto.parseBirthDate(user.getSocialNum()),
                user.getPhone(),
                user.getCreatedAt().toLocalDate(),
                user.getName(),
                user.getProfileImage(),
                user.getAnnualLeave()
        );
    }
}
