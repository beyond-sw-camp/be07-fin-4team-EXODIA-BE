package com.example.exodia.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDeleteDto {
    private String userId;
    private String deletedBy;
    private String reason;
}
