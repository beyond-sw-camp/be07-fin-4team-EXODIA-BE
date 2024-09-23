package com.example.exodia.evalution.controller;

import com.example.exodia.evalution.service.EvalutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* 인사 평가 컨트롤러 */
@RestController
@RequestMapping("/evalution")
public class EvalutionController {

    @Autowired
    private final EvalutionService evalutionService;

    public EvalutionController(EvalutionService evalutionService) {
        this.evalutionService = evalutionService;
    }

}
