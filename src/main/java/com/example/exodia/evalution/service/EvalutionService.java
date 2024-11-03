package com.example.exodia.evalution.service;

import com.example.exodia.evalution.domain.Evalution;
import com.example.exodia.evalution.dto.EvalutionDto;
import com.example.exodia.evalution.repository.EvalutionRepository;
import com.example.exodia.evalutionFrame.subevalution.domain.SubEvalution;
import com.example.exodia.evalutionFrame.subevalution.repository.SubEvalutionRepository;
import com.example.exodia.evalutionavg.repository.EvalutionAvgRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvalutionService {
    private final EvalutionRepository evalutionRepository;
    private final SubEvalutionRepository subEvalutionRepository;
    private final UserRepository userRepository;
    private final EvalutionAvgRepository evalutionAvgRepository;

    public EvalutionService(EvalutionRepository evalutionRepository, SubEvalutionRepository subEvalutionRepository, UserRepository userRepository, EvalutionAvgRepository evalutionAvgRepository) {
        this.evalutionRepository = evalutionRepository;
        this.subEvalutionRepository = subEvalutionRepository;
        this.userRepository = userRepository;
        this.evalutionAvgRepository = evalutionAvgRepository;
    }

    @Transactional
    public Evalution createEvalution(EvalutionDto evalutionDto) {
        // 현재 인증된 사용자 가져오기 (평가자)
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User evaluator = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

        // 평가할 소분류 조회
        SubEvalution subEvalution = subEvalutionRepository.findById(evalutionDto.getSubEvalutionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소분류: " + evalutionDto.getSubEvalutionId()));

        // 평가 대상자 조회
        User target = userRepository.findByUserNum(evalutionDto.getTargetUserNum())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대상자 ID: " + evalutionDto.getTargetUserNum()));

        // 평가 권한 확인
        if (!canEvaluate(evaluator, target)) {
            throw new RuntimeException("평가할 권한이 없습니다.");
        }

        // DTO를 통해 엔티티 생성
        Evalution evalution = evalutionDto.toEntity(subEvalution, evaluator, target);
        return evalutionRepository.save(evalution);
    }

    // 평가 권한을 확인하는 메서드
    private boolean canEvaluate(User evaluator, User target) {
        // 자기 자신 평가 권한
        if (evaluator.equals(target)) {
            return true;
        }
        // 같은 부서의 팀장인지 확인
        if (evaluator.getDepartment().equals(target.getDepartment())
                && evaluator.getPosition().getName().equals("팀장")) {
            return true;
        }


//        // 상위 부서의 명칭..인지 확인
//        Department parentDepartment = target.getDepartment().getParentDepartment();
//        while (parentDepartment != null) {
//            if (evaluator.getDepartment().equals(parentDepartment)
//                    && evaluator.getPosition().getName().equals("소장")) {
//                return true;
//            }
//            parentDepartment = parentDepartment.getParentDepartment();
//        }
        return false;
    }

    /* 평가 조회 */
    @Transactional(readOnly = true)
    public List<EvalutionDto> getAllEvalutions() {
        List<Evalution> evalutions = evalutionRepository.findAll();
        return evalutions.stream().map(Evalution::fromEntity).collect(Collectors.toList());
    }


    @Transactional
    public List<Evalution> batchCreateEvalutions(List<EvalutionDto> evalutionDtos) {
        // 현재 인증된 사용자 가져오기 (평가자)
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User evaluator = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

        // 평가를 생성하는 로직
        List<Evalution> createdEvalutions = evalutionDtos.stream().map(dto -> {
            // 평가할 소분류 조회
            SubEvalution subEvalution = subEvalutionRepository.findById(dto.getSubEvalutionId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소분류: " + dto.getSubEvalutionId()));

            // targetUserNum이 유효한지 확인
            if (dto.getTargetUserNum() == null || dto.getTargetUserNum().isEmpty()) {
                throw new IllegalArgumentException("Target UserNum must not be null or empty");
            }

            // 평가 대상자 조회
            User target = userRepository.findByUserNum(dto.getTargetUserNum())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대상자 ID: " + dto.getTargetUserNum()));

            // 중복성 검증
            boolean isDuplicate = evalutionRepository.existsByEvaluatorAndTargetAndSubEvalution(
                    evaluator, target, subEvalution
            );
            if (isDuplicate) {
                throw new RuntimeException("중복된 평가: 이미 해당 소분류에 대한 평가가 존재합니다.");
            }

            // 평가 권한 확인 (자신 또는 같은 부서 팀원인 경우만)
            if (!canEvaluate(evaluator, target)) {
                throw new RuntimeException("평가할 권한이 없습니다.");
            }

            // DTO를 통해 엔티티 생성
            return dto.toEntity(subEvalution, evaluator, target);
        }).collect(Collectors.toList());

        return evalutionRepository.saveAll(createdEvalutions);
    }

//    @Transactional
//    public void calculateAndSaveEvaluatorTargetScore(Long evalutionId) {
//        Evalution evalution = evalutionRepository.findById(evalutionId)
//                .orElseThrow(() -> new RuntimeException("존재하지 않는 평가입니다."));
//
//        int scoreValue = evalution.getScore().getValue(); // 평가 점수 값
//        User evaluator = evalution.getEvaluator();
//        User targetUser = evalution.getTarget();
//
//        // 평가자와 대상자 조합으로 총 평가 점수 조회 또는 새로 생성
//        EvalutionAvg evalutionAvg = evalutionAvgRepository.findByEvaluatorIdAndTargetUserId(evaluator.getId(), targetUser.getId())
//                .orElse(EvalutionAvg.builder()
//                        .evaluator(evaluator)
//                        .targetUser(targetUser)
//                        .totalScore(0)
//                        .build());
//
//        // 총 점수와 횟수 업데이트
//        evalutionAvg.setTotalScore(evalutionAvg.getTotalScore() + scoreValue);
//        evalutionAvgRepository.save(evalutionAvg);
//    }
}