//package com.example.exodia.evalutionavg.service;
//
//import com.example.exodia.evalution.domain.Evalution;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//public class EvalutionService {
//
//    @Transactional
//    public void calculateAndSaveAvgScoreForUser(Long targetUserId, Long evaluatorId) {
//        // 특정 대상 사용자와 평가자, 평가 유형에 대한 평가 조회
//        List<Evalution> evaluations = evalutionRepository.findByTargetUserIdAndEvaluatorIdAndEvaluationType(targetUserId, evaluatorId, evaluationType);
//
//        if (evaluations.isEmpty()) {
//            throw new RuntimeException("평가가 존재하지 않습니다.");
//        }
//
//        // 평균 점수 계산
//        double avgScore = evaluations.stream()
//                .mapToDouble(eval -> eval.getScore().getValue())
//                .average()
//                .orElse(0.0);
//
//        // 총 평가 개수 계산
//        int totalCount = evaluations.size();
//
//        // 기존 평균 점수 정보가 있으면 업데이트, 없으면 새로 저장
//        EvalutionAvg evalutionAvg = evalutionAvgRepository.findByTargetUserIdAndEvaluatorIdAndEvaluationType(targetUserId, evaluatorId, evaluationType)
//                .orElse(EvalutionAvg.builder()
//                        .targetUser(evaluations.get(0).getTarget())
//                        .evaluator(evaluations.get(0).getEvaluator())
//                        .evaluationType(evaluationType)
//                        .build());
//
//        evalutionAvg.setAvgScore(avgScore);
//        evalutionAvg.setTotalCount(totalCount);
//        evalutionAvgRepository.save(evalutionAvg);
//    }
//
//}
