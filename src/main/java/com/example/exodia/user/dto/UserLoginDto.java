package com.example.exodia.user.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginDto {
    private String userNum;
    private String password;
}
