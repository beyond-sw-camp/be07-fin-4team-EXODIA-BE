package com.example.exodia.evalutionFrame.subevalution.dto;

import com.example.exodia.evalutionFrame.evalutionMiddle.domain.Evalutionm;
import com.example.exodia.evalutionFrame.subevalution.domain.SubEvalution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubEvalutionResponseDto {
    private Long subEvalutionId;      // 소분류 ID
    private String subEvalutionContent; // 소분류 내용
    private String midCategoryName;   // 중분류 이름
    private String bigCategoryName;   // 대분류 이름
    private Long evalutionmId; // 중분류 아이디

    public static SubEvalutionResponseDto fromEntity(SubEvalution subEvalution) {
        return SubEvalutionResponseDto.builder()
                .subEvalutionId(subEvalution.getId())
                .subEvalutionContent(subEvalution.getContent())
                .midCategoryName(subEvalution.getEvalutionm().getMName())
                .bigCategoryName(subEvalution.getEvalutionm().getEvalutionb().getBName())
                .evalutionmId(subEvalution.getEvalutionm().getId())
                .build();
    }

    public static SubEvalutionResponseDto fromEvalutionmWithoutSub(Evalutionm evalutionm) {
        return SubEvalutionResponseDto.builder()
                .subEvalutionId(builder().subEvalutionId)
                .subEvalutionContent(null)
                .midCategoryName(evalutionm.getMName())
                .bigCategoryName(evalutionm.getEvalutionb().getBName())
                .evalutionmId(evalutionm.getId())
                .build();
    }
}
