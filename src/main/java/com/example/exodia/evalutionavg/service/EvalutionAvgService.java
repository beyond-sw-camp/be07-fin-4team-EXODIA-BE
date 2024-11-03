package com.example.exodia.evalutionavg.service;

import com.example.exodia.evalution.domain.Evalution;
import com.example.exodia.evalution.repository.EvalutionRepository;
import com.example.exodia.evalutionFrame.subevalution.domain.SubEvalution;
import com.example.exodia.evalutionFrame.subevalution.repository.SubEvalutionRepository;
import com.example.exodia.evalutionavg.domain.EvalutionAvg;
import com.example.exodia.evalutionavg.repository.EvalutionAvgRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;

@Service
public class EvalutionAvgService {

	private final UserRepository userRepository;
	private final SubEvalutionRepository subEvalutionRepository;
	private final EvalutionRepository evalutionRepository;
	private final EvalutionAvgRepository evalutionAvgRepository;

	public EvalutionAvgService(UserRepository userRepository, SubEvalutionRepository subEvalutionRepository,
		EvalutionRepository evalutionRepository, EvalutionAvgRepository evalutionAvgRepository) {
		this.userRepository = userRepository;
		this.subEvalutionRepository = subEvalutionRepository;
		this.evalutionRepository = evalutionRepository;
		this.evalutionAvgRepository = evalutionAvgRepository;
	}

	@Transactional
	public double calculateAndSaveAvgScoreForUser(SubEvalution subEvalution) {

		// 하나의 항목에 대한 점수 저장
		// 항목별 평기점순
		List<Evalution> evaluations = evalutionRepository.findBySubEvalution(subEvalution);

		if (evaluations.isEmpty()) {
			return 0.0;
		}

		double totalScore = evaluations.stream()
			.mapToDouble(evaluation -> evaluation.getScore().getValue())
			.sum();

		evalutionAvgRepository.save(EvalutionAvg.builder()
			.subEvalution(subEvalution)
			.targetUser(subEvalution.getUser())
			.avgScore(totalScore/evaluations.size())
			.build());

		return totalScore;
	}

	@Transactional
	public void getUserAndSubEvaluations() {
		// 특정 사용자에 대한 평가 리스트 모두 조회
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new EntityNotFoundException("회원 정보가 존재하지 않습니다."));

		List<SubEvalution> subEvalutions = subEvalutionRepository.findByUser(user);

		for(SubEvalution sub : subEvalutions){
			calculateAndSaveAvgScoreForUser(sub);
		}
	}

}
