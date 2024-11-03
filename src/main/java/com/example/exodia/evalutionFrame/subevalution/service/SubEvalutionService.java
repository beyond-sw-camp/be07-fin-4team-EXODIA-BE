package com.example.exodia.evalutionFrame.subevalution.service;

import com.example.exodia.department.domain.Department;
import com.example.exodia.evalutionFrame.subevalution.dto.SubEvalutionDto;
import com.example.exodia.evalutionFrame.evalutionMiddle.domain.Evalutionm;
import com.example.exodia.evalutionFrame.evalutionMiddle.repository.EvalutionmRepository;
import com.example.exodia.evalutionFrame.subevalution.domain.SubEvalution;
import com.example.exodia.evalutionFrame.subevalution.dto.SubEvalutionResponseDto;
import com.example.exodia.evalutionFrame.subevalution.dto.SubEvalutionUpdateDto;
import com.example.exodia.evalutionFrame.subevalution.dto.SubEvalutionWithUserDetailsDto;
import com.example.exodia.evalutionFrame.subevalution.repository.SubEvalutionRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;

import org.hibernate.Hibernate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SubEvalutionService {
	private final SubEvalutionRepository subEvalutionRepository; // 소분류
	private final EvalutionmRepository evalutionmRepository; // 중분류
	private final UserRepository userRepository;

	public SubEvalutionService(SubEvalutionRepository subEvalutionRepository, EvalutionmRepository evalutionmRepository,
		UserRepository userRepository) {
		this.subEvalutionRepository = subEvalutionRepository;
		this.evalutionmRepository = evalutionmRepository;
		this.userRepository = userRepository;
	}

	/* 소분류 content 생성 */
	@Transactional
	public SubEvalution createSubEvalution(SubEvalutionDto subEvalutionDto) {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		Evalutionm evalutionm = evalutionmRepository.findById(subEvalutionDto.getEvalutionmId())
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 중분류 : " + subEvalutionDto.getEvalutionmId()));

		SubEvalution subEvalution = subEvalutionDto.toEntity(evalutionm, user);

		return subEvalutionRepository.save(subEvalution);
	}

	/* 소분류 조회 */
	@Transactional(readOnly = true)
	public List<SubEvalution> getAllSubEvalutionsForUser() {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		// 사용자가 작성한 모든 소분류 조회
		return subEvalutionRepository.findByUser(user);
	}

	@Transactional
	public SubEvalution updateSubEvalution(SubEvalutionUpdateDto subEvalutionUpdateDto) {
		SubEvalution subEvalution = subEvalutionRepository.findById(subEvalutionUpdateDto.getId())
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소분류 아이디 :  " + subEvalutionUpdateDto.getId()));

		// 내용 업데이트
		subEvalution.setContent(subEvalutionUpdateDto.getContent());

		// 수정된 소분류 저장
		return subEvalutionRepository.save(subEvalution);
	}

	@Transactional
	public void deleteSubEvalution(Long id) {
		SubEvalution subEvalution = subEvalutionRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소분류 아이디 : " + id));

		subEvalutionRepository.delete(subEvalution);
	}

	/* 리스트 */
	/* 소분류 조회(대*중분류명 추가) */
	@Transactional(readOnly = true)
	public List<SubEvalutionResponseDto> getAllSubEvalutionsWithCategoriesForUser() {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));
		// 사용자가 작성한 모든 소분류 조회
		List<SubEvalution> subEvalutions = subEvalutionRepository.findByUser(user);
		// 중분류, 대분류 정보만 반환(소분류가 없는 경우)
		List<SubEvalutionResponseDto> evalutionDtos = evalutionmRepository.findAll().stream()
			.map(SubEvalutionResponseDto::fromEvalutionmWithoutSub)
			.collect(Collectors.toList());

		// 소분류 넣으면 null -> 데이터 처리
		subEvalutions.forEach(subEvalution -> {
			evalutionDtos.stream()
				.filter(dto -> dto.getEvalutionmId().equals(subEvalution.getEvalutionm().getId()))
				.findFirst()
				.ifPresent(dto -> {
					dto.setSubEvalutionContent(subEvalution.getContent());
					dto.setSubEvalutionId(subEvalution.getId());
				});

		});
		return evalutionDtos;
	}

	/* 팀장이 자신의 팀원의 평가 조회 */
	@Transactional(readOnly = true)
	public List<SubEvalutionWithUserDetailsDto> getTeamMembersSubEvalutions(String userNum) {
		String loginUser = SecurityContextHolder.getContext().getAuthentication().getName();
		User teamLeader = userRepository.findByUserNum(loginUser)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		if (!teamLeader.getPosition().getName().equals("팀장")) {
			throw new RuntimeException("팀장만 접근할 수 있습니다.");
		}

		User selectedUser = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));
		if (!selectedUser.getDepartment().getId().equals(teamLeader.getDepartment().getId())) {
			throw new RuntimeException("해당 팀원의 평가 정보에 접근할 수 없습니다.");
		}

		List<SubEvalution> subEvalutions = subEvalutionRepository.findByUser(selectedUser);
		List<SubEvalutionWithUserDetailsDto> dto = new ArrayList<>();
		subEvalutions.forEach(subEvalution -> {
			dto.add(SubEvalutionWithUserDetailsDto.fromEntity(subEvalution, selectedUser));
		});

		return dto;
	}

	/* 한번에 리스로 저장하는 로직 */
	@Transactional
	public List<SubEvalution> createMultipleSubEvalutions(List<SubEvalutionDto> subEvalutionDtoList) {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 사원입니다"));

		List<SubEvalution> savedSubEvalutions = new ArrayList<>();
		for (SubEvalutionDto dto : subEvalutionDtoList) {
			try {
				Optional<SubEvalution> existingSubEvalution = subEvalutionRepository
					.findByUserIdAndEvalutionmId(user.getId(), dto.getEvalutionmId());

				if (existingSubEvalution.isPresent()) {
					continue;
				}

				Evalutionm evalutionm = evalutionmRepository.findById(dto.getEvalutionmId())
					.orElseThrow(() -> new RuntimeException("존재하지 않는 중분류입니다."));

				SubEvalution subEvalution = dto.toEntity(evalutionm, user);
				savedSubEvalutions.add(subEvalutionRepository.save(subEvalution));

			} catch (Exception e) {
				System.err.println("Error occurred while saving SubEvalution: " + e.getMessage());
				e.printStackTrace();
				throw new RuntimeException("SubEvalution 저장 중 오류가 발생했습니다.");
			}
		}
		return savedSubEvalutions;
	}
}
