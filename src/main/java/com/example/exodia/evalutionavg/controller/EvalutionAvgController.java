package com.example.exodia.evalutionavg.controller;

import java.io.IOException;
import java.util.List;

import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.evalution.service.EvalutionService;
import com.example.exodia.evalutionFrame.subevalution.dto.SubEvalutionWithUserDetailsDto;
import com.example.exodia.evalutionavg.dto.EvaluationAvgResDto;
import com.example.exodia.evalutionavg.service.EvalutionAvgService;
import com.example.exodia.submit.domain.Submit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;

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
	public ResponseEntity<?> calculateEvaluatorTargetScore() {
		//  evalutionAvgService.getUserAndSubEvaluations();
		// return ResponseEntity.ok().build();
		try {
			List<EvaluationAvgResDto> dtos = evalutionAvgService.getUserAndSubEvaluations();
			return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "인사평가 평균 계산 성공", dtos));
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()),
				HttpStatus.BAD_REQUEST);
		}
	}

}
