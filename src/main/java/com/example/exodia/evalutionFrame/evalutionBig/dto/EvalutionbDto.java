package com.example.exodia.evalutionFrame.evalutionBig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvalutionbDto {
    private String bName; // 대분류 명

    public void setBName(String bName) {
        this.bName = bName;
    }
}
