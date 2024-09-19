package com.example.exodia.userDelete.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDeleteDto {
    private String deletedBy;
    private String reason;
    private String userNum;
}

