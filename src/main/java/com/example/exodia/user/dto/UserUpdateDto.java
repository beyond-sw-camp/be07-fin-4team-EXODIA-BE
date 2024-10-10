package com.example.exodia.user.dto;

import com.example.exodia.user.domain.HireType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {
    private String userNum;
    private String name;
    private String email;
    private String address;
    private String phone;
    private String profileImage;
    private HireType hireType;
    private Long departmentId;
    private Long positionId;
    private int annualLeave;
    private String password;
    private String socialNum;
}
