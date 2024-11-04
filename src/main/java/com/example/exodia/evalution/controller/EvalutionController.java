package com.example.exodia.evalution.controller;

import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.evalution.domain.Evalution;
import com.example.exodia.evalution.dto.EvalutionDto;
import com.example.exodia.evalution.service.EvalutionService;
import com.example.exodia.submit.domain.SubmitLine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/* 인사 평가 컨트롤러 */
@RestController
@RequestMapping("/evalution")
public class EvalutionController {
    @Autowired
    private final EvalutionService evalutionService;

    public EvalutionController(EvalutionService evalutionService) {
        this.evalutionService = evalutionService;
    }

    @PostMapping("/create")
    public ResponseEntity<Evalution> createEvalution(@RequestBody EvalutionDto evalutionDto) {
        Evalution createdEvalution = evalutionService.createEvalution(evalutionDto);
        return ResponseEntity.status(201).body(createdEvalution);
    }

    @GetMapping("/list")
    public ResponseEntity<List<EvalutionDto>> getAllEvalutions() {
        List<EvalutionDto> evalutionList = evalutionService.getAllEvalutions();
        return ResponseEntity.ok(evalutionList);
    }

    @PostMapping("/batch-create")
    public ResponseEntity<?> batchCreateEvalutions(@RequestBody List<EvalutionDto> evalutionDtos) {
       try {
            List<Evalution> createdEvalutions = evalutionService.batchCreateEvalutions(evalutionDtos);
            return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "평가 성공", createdEvalutions));
        }catch (IllegalArgumentException e) {
           e.printStackTrace();
           return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);

       }
    }

}
