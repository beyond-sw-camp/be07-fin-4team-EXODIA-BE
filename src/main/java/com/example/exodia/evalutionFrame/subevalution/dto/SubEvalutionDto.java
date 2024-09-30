package com.example.exodia.evalutionFrame.subevalution.dto;

import com.example.exodia.evalutionFrame.evalutionMiddle.domain.Evalutionm;
import com.example.exodia.evalutionFrame.subevalution.domain.SubEvalution;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubEvalutionDto {
    private Long evalutionmId;
    private String content;

    public SubEvalution toEntity(Evalutionm evalutionm, User user) {
        return SubEvalution.builder()
                .content(this.content)
                .evalutionm(evalutionm)
                .user(user)
                .build();
    }
}
