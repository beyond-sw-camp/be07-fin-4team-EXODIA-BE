package com.example.exodia.user.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {
    private String name;
    private String email;
    private String phone;
    private String address;
    private Long departmentId;
    private Long positionId;
    private int annualLeave;
}