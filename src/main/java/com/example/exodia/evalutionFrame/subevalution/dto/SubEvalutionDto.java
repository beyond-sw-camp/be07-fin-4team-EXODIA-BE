package com.example.exodia.evalutionFrame.subevalution.dto;

import com.example.exodia.evalutionFrame.evalutionMiddle.domain.Evalutionm;
import com.example.exodia.evalutionFrame.subevalution.domain.SubEvalution;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.dto.UserDto;
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
    private UserDto user;

    public SubEvalution toEntity(Evalutionm evalutionm, User user) {
        return SubEvalution.builder()
                .content(this.content)
                .evalutionm(evalutionm)
                .user(user)
                .build();
    }

    public static SubEvalutionDto fromEntity(SubEvalution subEvalution) {
        return SubEvalutionDto.builder()
                .evalutionmId(subEvalution.getEvalutionm().getId())
                .content(subEvalution.getContent())
                .user(UserDto.fromEntity(subEvalution.getUser()))
                .build();
    }
}
