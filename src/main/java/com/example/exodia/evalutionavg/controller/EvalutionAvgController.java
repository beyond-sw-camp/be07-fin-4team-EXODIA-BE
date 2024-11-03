package com.example.exodia.evalutionavg.controller;

import com.example.exodia.evalution.service.EvalutionService;
import com.example.exodia.evalutionavg.domain.EvalutionAvg;
import com.example.exodia.evalutionavg.service.EvalutionAvgService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/evalution-avg")
public class EvalutionAvgController {
    private final EvalutionService evalutionService;
    private final EvalutionAvgService evalutionAvgService;

    public EvalutionAvgController(EvalutionService evalutionService, EvalutionAvgService evalutionAvgService) {
        this.evalutionService = evalutionService;
        this.evalutionAvgService = evalutionAvgService;
    }


//    // 특정 평가를 기반으로 평가자-대상자 간 총점 저장
//    @PostMapping("/calculate/{evalutionId}")
//    public ResponseEntity<Void> calculateEvaluatorTargetScore(@PathVariable Long evalutionId) {
//        evalutionService.calAndSaveEvaluatorAvg(evalutionId);
//        return ResponseEntity.ok().build();
//    }

    @PostMapping("/calculate")
       public ResponseEntity<Void> calculateEvaluatorTargetScore() {
            evalutionAvgService.getUserAndSubEvaluations();
           return ResponseEntity.ok().build();
       }

}
