package com.example.exodia.evalutionFrame.subevalution.dto;

import com.example.exodia.evalution.domain.Evalution;
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
public class SubEvalutionWithUserDetailsDto {
    private Long subEvalutionId;
    private String subEvalutionContent;
    private String midCategoryName;
    private String bigCategoryName;
    private Long evalutionmId;
    private String userName;
    private String userPosition;
    private String score;

    public static SubEvalutionWithUserDetailsDto fromEntity(SubEvalution subEvalution, User user, Evalution evalution) {
        return SubEvalutionWithUserDetailsDto.builder()
                .subEvalutionId(subEvalution.getId())
                .subEvalutionContent(subEvalution.getContent())
                .midCategoryName(subEvalution.getEvalutionm().getMName())
                .bigCategoryName(subEvalution.getEvalutionm().getEvalutionb().getBName())
                .evalutionmId(subEvalution.getEvalutionm().getId())
                .userName(user.getName())
                .userPosition(user.getPosition().getName())
                .score(evalution == null? "" :evalution.getScore().toString())
                .build();
    }
}

