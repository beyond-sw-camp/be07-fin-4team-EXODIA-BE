package com.example.exodia.submit.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.exodia.common.service.KafkaProducer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.exodia.position.domain.Position;
import com.example.exodia.position.repository.PositionRepository;
import com.example.exodia.submit.domain.Submit;
import com.example.exodia.submit.domain.SubmitLine;
import com.example.exodia.submit.domain.SubmitStatus;
import com.example.exodia.submit.domain.SubmitType;
import com.example.exodia.submit.dto.SubmitListResDto;
import com.example.exodia.submit.dto.SubmitSaveReqDto;
import com.example.exodia.submit.dto.SubmitStatusUpdateDto;
import com.example.exodia.submit.repository.SubmitLineRepository;
import com.example.exodia.submit.repository.SubmitRepository;
import com.example.exodia.submit.repository.SubmitTypeRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SubmitService {

	private final SubmitRepository submitRepository;
	private final UserRepository userRepository;
	private final PositionRepository positionRepository;
	private final SubmitLineRepository submitLineRepository;
	private final KafkaProducer kafkaProducer;
	private final SubmitTypeRepository submitTypeRepository;

	public SubmitService(SubmitRepository submitRepository, UserRepository userRepository,
                         PositionRepository positionRepository, SubmitLineRepository submitLineRepository,SubmitTypeRepository submitTypeRepository, KafkaProducer kafkaProducer) {
		this.submitRepository = submitRepository;
		this.userRepository = userRepository;
		this.positionRepository = positionRepository;
		this.submitLineRepository = submitLineRepository;
    this.kafkaProducer = kafkaProducer;
		this.submitTypeRepository = submitTypeRepository;
	}

	// 	결재라인 등록
	public Submit createSubmit(SubmitSaveReqDto dto) {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

		User submitUser = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new EntityNotFoundException("회원정보가 존재하지 않습니다."));

		Submit submit = dto.toEntity(submitUser);
		SubmitLine submitLine = SubmitLine.builder().build();

		for (SubmitSaveReqDto.SubmitUserDto userDto : dto.getSubmitUserDtos()) {
			Position position = positionRepository.findByName(userDto.getPosition())
				.orElseThrow(() -> new EntityNotFoundException("직급 정보가 존재하지 않습니다."));

			User user = userRepository.findByNameAndPosition(userDto.getUserName(), position)
				.orElseThrow(() -> new EntityNotFoundException("회원정보가 존재하지 않습니다."));

			submitLine = dto.toLineEntity(user);

			submit.getSubmitLines().add(submitLine);
			submitLine.updateSubmit(submit);

			// Kafka 이벤트 전송
			String message = String.format("%s님에게 %s 결재 요청이 도착했습니다.", user.getName(), dto.getSubmitType());
			kafkaProducer.sendSubmitNotification("submit-events", user.getUserNum(), message);
		}

		submitRepository.save(submit);
		submitLineRepository.save(submitLine);

		return submit;
	}

	// 결재 상태 변경
	public List<SubmitLine> updateSubmit(SubmitStatusUpdateDto dto) throws IOException, EntityNotFoundException {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();    // 사용자
		List<SubmitLine> submitLines = submitLineRepository.findBySubmitIdOrderByUserNumDesc(dto.getSubmitId());
		Submit submit = submitRepository.findById(dto.getSubmitId())
			.orElseThrow(() -> new EntityNotFoundException("결재 정보가 존재하지 않습니다."));

		for (SubmitLine line : submitLines) {
			if (line.getUserNum().compareTo(userNum) == 0) {
				if (line.getSubmitStatus() == SubmitStatus.WAITING) {
					if (dto.getStatus() == SubmitStatus.REJECT) {
						// 	관련된 모든 결재 REJECT
						dto.chkReason();
						submit.updateStatus(SubmitStatus.REJECT, dto.getReason());
						changeToReject(dto.getSubmitId());
					} else if (dto.getStatus() == SubmitStatus.ACCEPT) {
						line.updateStatus(dto.getStatus());
						submitLineRepository.save(line);
					}
				} else if (line.getSubmitStatus() == SubmitStatus.REJECT) {
					throw new IOException("이미 반려 된 결재입니다.");
				}
				break;
			} else if (line.getUserNum().compareTo(userNum) > 0) {
				// 	이전 결재자
				if (line.getSubmitStatus() == SubmitStatus.ACCEPT) {
					continue;
				} else if (line.getSubmitStatus() == SubmitStatus.WAITING) {
					throw new IOException("이전 결재자의 결재가 필요합니다.");
				}
			}
		}
		return submitLines;
	}

	public void changeToReject(Long submitId) {
		// 히스토리 모든 문서들 REJECT
		List<SubmitLine> submitLines = submitLineRepository.findBySubmitIdOrderByUserNumDesc(submitId);
		for(SubmitLine line : submitLines) {
			line.updateStatus(SubmitStatus.REJECT);
			submitLineRepository.save(line);
		}
	}

	// 결재 타입 리스트 전체 조회
	public List<?> getTypeList(){
		List<SubmitType> types = submitTypeRepository.findAll();
		return types.stream()
			.map(SubmitType::getTypeName)
			.collect(Collectors.toList());
	}

	// 나에게 요청 들어온 결재 리스트 조회
	public List<?> getSubmitList(){
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

		List<SubmitLine> submitLines = submitLineRepository.findAllByUserNum(userNum);
		List<SubmitListResDto> mySubmitList = submitLines.stream()
			.map(submitLine -> {
				User user = submitLine.getSubmit().getUser();
				return new SubmitListResDto().fromLineEntity(user, submitLine);
			})
			.collect(Collectors.toList());
		return mySubmitList;
	}

	// 내가 요청한 결재 리스트 조회
	public List<?> getMySubmitList(){
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user=  userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new EntityNotFoundException("회원 정보가 존재하지 않습니다."));

		List<Submit> submits = submitRepository.findAllByUser(user);

		List<SubmitListResDto> mySubmitList = submits.stream()
			.map(submit -> {
				return new SubmitListResDto().fromEntity(submit);
			})
			.collect(Collectors.toList());
		return mySubmitList;
	}



}
