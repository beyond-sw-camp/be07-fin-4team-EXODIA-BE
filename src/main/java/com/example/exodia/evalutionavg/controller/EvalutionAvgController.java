package com.example.exodia.evalutionavg.controller;

import com.example.exodia.evalution.service.EvalutionService;
import com.example.exodia.evalutionavg.domain.EvalutionAvg;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/evalution-avg")
public class EvalutionAvgController {
    private final EvalutionService evalutionService;

    public EvalutionAvgController(EvalutionService evalutionService) {
        this.evalutionService = evalutionService;
    }


//    // 특정 평가를 기반으로 평가자-대상자 간 총점 저장
//    @PostMapping("/calculate/{evalutionId}")
//    public ResponseEntity<Void> calculateEvaluatorTargetScore(@PathVariable Long evalutionId) {
//        evalutionService.calAndSaveEvaluatorAvg(evalutionId);
//        return ResponseEntity.ok().build();
//    }
}
