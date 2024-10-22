package com.example.exodia.submit.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.exodia.board.domain.Board;
import com.example.exodia.board.domain.Category;
import com.example.exodia.board.dto.BoardSaveReqDto;
import com.example.exodia.board.repository.BoardRepository;
import com.example.exodia.board.service.BoardAutoUploadService;
import com.example.exodia.common.service.KafkaProducer;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.exodia.department.domain.Department;
import com.example.exodia.department.repository.DepartmentRepository;
import com.example.exodia.position.domain.Position;
import com.example.exodia.position.repository.PositionRepository;
import com.example.exodia.submit.domain.Submit;
import com.example.exodia.submit.domain.SubmitLine;
import com.example.exodia.submit.domain.SubmitStatus;
import com.example.exodia.submit.domain.SubmitType;
import com.example.exodia.submit.dto.SubmitDetResDto;
import com.example.exodia.submit.dto.SubmitLineResDto;
import com.example.exodia.submit.dto.SubmitListResDto;
import com.example.exodia.submit.dto.SubmitSaveReqDto;
import com.example.exodia.submit.dto.SubmitStatusUpdateDto;
import com.example.exodia.submit.repository.SubmitLineRepository;
import com.example.exodia.submit.repository.SubmitRepository;
import com.example.exodia.submit.repository.SubmitTypeRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SubmitService {

	private final SubmitRepository submitRepository;
	private final UserRepository userRepository;
	private final PositionRepository positionRepository;
	private final SubmitLineRepository submitLineRepository;
	private final KafkaProducer kafkaProducer;
	private final SubmitTypeRepository submitTypeRepository;
	private final DepartmentRepository departmentRepository;
	private final BoardRepository boardRepository;
	private final BoardAutoUploadService boardAutoUploadService;

	public SubmitService(SubmitRepository submitRepository, UserRepository userRepository,
                         PositionRepository positionRepository, SubmitLineRepository submitLineRepository,
                         SubmitTypeRepository submitTypeRepository, KafkaProducer kafkaProducer,
                         DepartmentRepository departmentRepository, BoardRepository boardRepository, BoardAutoUploadService boardAutoUploadService) {
		this.submitRepository = submitRepository;
		this.userRepository = userRepository;
		this.positionRepository = positionRepository;
		this.submitLineRepository = submitLineRepository;
		this.kafkaProducer = kafkaProducer;
		this.submitTypeRepository = submitTypeRepository;
		this.departmentRepository = departmentRepository;
		this.boardRepository = boardRepository;
        this.boardAutoUploadService = boardAutoUploadService;
    }

	// 	결재라인 등록
	@Transactional
	public Submit createSubmit(SubmitSaveReqDto dto) {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

		User submitUser = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new EntityNotFoundException("회원정보가 존재하지 않습니다."));

		Submit submit = dto.toEntity(submitUser);
		SubmitLine submitLine = SubmitLine.builder().build();

		for (SubmitSaveReqDto.SubmitUserDto userDto : dto.getSubmitUserDtos()) {
			Position position = positionRepository.findById(userDto.getPosition())
				.orElseThrow(() -> new EntityNotFoundException("직급 정보가 존재하지 않습니다."));

			User user = userRepository.findByNameAndPosition(userDto.getUserName(), position)
				.orElseThrow(() -> new EntityNotFoundException("회원정보가 존재하지 않습니다."));

			submitLine = dto.toLineEntity(user);

			submit.getSubmitLines().add(submitLine);
			submitLine.updateSubmit(submit);
		}

		submitRepository.save(submit);
		submitLineRepository.save(submitLine);

		for (SubmitLine line : submit.getSubmitLines()) {
			String approverName = line.getUserNum();
			kafkaProducer.sendSubmitNotification("submit-events", approverName, line.getUserNum(),
				LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM.dd")));
		}

		return submit;
	}

	// 결재 상태 변경
//	@Transactional
//	public List<SubmitLine> updateSubmit(SubmitStatusUpdateDto dto) throws IOException, EntityNotFoundException {
//		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();    // 사용자
//		// 이전 결재자들의 결재 상태를 확인하기 위해 필요
//		List<SubmitLine> submitLines = submitLineRepository.findBySubmitIdOrderByUserPositionId(
//				dto.getSubmitId());    // 직급 순서대로 가져오는걸로
//		Submit submit = submitRepository.findById(dto.getSubmitId())
//				.orElseThrow(() -> new EntityNotFoundException("결재 정보가 존재하지 않습니다."));
//
//		// 나의 결재 상태를 확인하기 위해서 필요
//
//		// 1. 내가 이미 처리한 결재인 경우
//		SubmitLine mySubmitLine = submitLineRepository.findBySubmitIdAndUserNum(dto.getSubmitId(), userNum);
//		if (mySubmitLine.getSubmitStatus() != SubmitStatus.WAITING) {
//			throw new IOException("이미 처리 된 결재입니다.");
//		}
//
//		int idx = 0;
//		for (SubmitLine submitLine : submitLines) {
//			if (!submitLine.getUserNum().equals(userNum)) {
//				// 2. 이전 결재자의 결재가 필요한 경우
//				if (submitLine.getSubmitStatus() == SubmitStatus.WAITING) {
//					throw new IOException("이전 결재자의 결재가 필요합니다.");
//				}
//			} else {
//				// REJECT
//				if (dto.getStatus() == SubmitStatus.REJECT) {
//					if (dto.getReason() == null) {
//						throw new IOException("반려 사유를 입력해주세요.");
//					} else {
//						// 	submitLine, submit 모든걸 reject로
//						changeToReject(dto.getSubmitId(), dto.getReason());
//					}
//				} else {
//					// 	ACCEPT
//					// 	subLine상태 바꾸기
//					// 	내가 최상단 결재자라면 submit상태도 바꾸기
//					submitLine.updateStatus(SubmitStatus.ACCEPT);
//					if (idx == submitLines.size() - 1) {
//						submit.updateStatus(SubmitStatus.ACCEPT,null);
//
//					if (submit.isUploadBoard()) {
//							Board board = dto.toEntity(submit);
//							boardRepository.save(board);
//					}
//
//					}
//				}
//			}
//			idx++;
//		}
//		return submitLines;
//	}

	@Transactional
	public List<SubmitLine> updateSubmit(SubmitStatusUpdateDto dto) throws IOException, EntityNotFoundException {

		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

		// 직급 순서대로 결재 라인 가져오기
		List<SubmitLine> submitLines = submitLineRepository.findBySubmitIdOrderByUserPositionId(dto.getSubmitId());

		Submit submit = submitRepository.findById(dto.getSubmitId())
				.orElseThrow(() -> new EntityNotFoundException("결재 정보를 찾을 수 없습니다."));

		SubmitLine mySubmitLine = submitLineRepository.findBySubmitIdAndUserNum(dto.getSubmitId(), userNum);
		if (mySubmitLine.getSubmitStatus() != SubmitStatus.대기중) {
			throw new IOException("이미 처리된 결재입니다.");
		}

		for (int i = 0; i < submitLines.size(); i++) {
			SubmitLine submitLine = submitLines.get(i);

			if (!submitLine.getUserNum().equals(userNum)) {
				// 이전 결재자의 결재가 필요한 경우
				if (submitLine.getSubmitStatus() == SubmitStatus.대기중) {
					throw new IOException("이전 결재자의 결재가 필요합니다.");
				}
			} else {
				// 결재 상태 처리
				if (dto.getStatus() == SubmitStatus.반려) {
					if (dto.getReason() == null || dto.getReason().isEmpty()) {
						throw new IOException("반려 사유를 입력해주세요.");
					}
					// 반려 처리
					changeToReject(dto.getSubmitId(), dto.getReason());
					break;
				} else if (dto.getStatus() == SubmitStatus.승인) {
					submitLine.updateStatus(SubmitStatus.승인);

					// 최상단 결재자인 경우
					if (i == submitLines.size() - 1) {
						submit.updateStatus(SubmitStatus.승인, null);
						checkVacationType(submit);
						// 경조사 신청서인 경우 자동 게시판 업로드 처리
						if ("경조사 신청서".equals(submit.getSubmitType()) && submit.isUploadBoard()) {
							boardAutoUploadService.checkAndUploadFamilyEvent(submit.getId());
						}
						break;
					} else {
						// 다음 결재자에게 알림 전송
						String nextUserNum = submitLines.get(i + 1).getUserNum();
						User nextUser = userRepository.findByUserNum(nextUserNum)
								.orElseThrow(() -> new EntityNotFoundException("회원 정보가 존재하지 않습니다."));

						kafkaProducer.sendSubmitNotification("submit-events", nextUser.getName(),
								nextUser.getUserNum(),
								LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM.dd")));
						break;
					}
				}
			}
		}
		return submitLines;
	}



	// 반려로 상태 변경
	@Transactional
	public void changeToReject(Long submitId, String reason) {
		// 히스토리 모든 문서들 REJECT
		List<SubmitLine> submitLines = submitLineRepository.findBySubmitIdOrderByUserNumDesc(submitId);
		for (SubmitLine line : submitLines) {
			line.updateStatus(SubmitStatus.반려);
		}
		Submit submit = submitRepository.findById(submitId)
			.orElseThrow(() -> new EntityNotFoundException("결재 정보가 존재하지 않습니다."));
		submit.updateStatus(SubmitStatus.반려, reason);
	}

	// 결재 타입 리스트 전체 조회
	public List<?> getTypeList() {
		List<SubmitType> types = submitTypeRepository.findAll();
		return types.stream()
			.map(SubmitType::getTypeName)
			.collect(Collectors.toList());
	}

	// 나에게 요청 들어온 결재 리스트 조회
	public List<?> getSubmitList() {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

		List<SubmitLine> submitLines = submitLineRepository.findAllByUserNumOrderByCreatedAtDesc(userNum);
		List<SubmitListResDto> mySubmitList = submitLines.stream()
			.map(submitLine -> {
				User user = submitLine.getSubmit().getUser();
				return new SubmitListResDto().fromLineEntity(user, submitLine);
			})
			.collect(Collectors.toList());
		return mySubmitList;
	}

	// 내가 요청한 결재 리스트 조회
	public List<?> getMySubmitList() {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new EntityNotFoundException("회원 정보가 존재하지 않습니다."));

		List<Submit> submits = submitRepository.findAllByUserOrderByCreatedAtDesc(user);

		List<SubmitListResDto> mySubmitList = submits.stream()
			.map(submit -> {
				return new SubmitListResDto().fromEntity(submit);
			})
			.collect(Collectors.toList());
		return mySubmitList;
	}

	// 결재 상세 조회
	public SubmitDetResDto getSubmitDetail(Long id) {
		Submit submit = submitRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("결재 정보가 존재하지 않습니다."));

		Department department = departmentRepository.findById(submit.getDepartment_id())
			.orElseThrow(() -> new EntityNotFoundException("부서 정보가 존재하지 않습니다."));

		List<SubmitLineResDto> dtos = new ArrayList<>();
		for (SubmitLine submitLine : submit.getSubmitLines()) {
			User user = userRepository.findByUserNum(submitLine.getUserNum())
				.orElseThrow(() -> new EntityNotFoundException("회원 정보가 존재하지 않습니다."));

			Position position = positionRepository.findById(user.getPosition().getId())
				.orElseThrow(() -> new EntityNotFoundException("직급 정보가 존재하지 않습니다."));

			dtos.add(SubmitLineResDto.builder().userName(user.getName()).positionName(position.getName()).build());
		}

		return submit.fromEntity(department.getName(), dtos);
	}

	// 결재 삭제
	@Transactional
	public void deleteSubmit(Long id) {
		Submit submit = submitRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("결재 정보가 존재하지 않습니다."));

		// 대기중 상태 일 때만 삭제 가능
		if (submit.getSubmitStatus() == SubmitStatus.대기중) {
			submit.softDelete();
		}
	}


	private void uploadBoardAutomatically(Submit submit) {
		User user = userRepository.findByUserNum(submit.getUserNum())
				.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		String title = "경조사 공지 - " + user.getDepartment().getName() + " " + user.getName();
		String content = submit.getContents();

		BoardSaveReqDto boardDto = BoardSaveReqDto.builder()
				.title(title)
				.content(content)
				.category(Category.FAMILY_EVENT)
				.userNum(submit.getUserNum())
				.build();

		Board board = boardDto.toEntity(user);
		boardRepository.save(board);
	}


	// 휴가 신청서면 휴가 차감
	// 신청서 종류에 따라 처리
	@Transactional
	public void checkVacationType(Submit submit) throws JsonProcessingException {
		String userNum = submit.getUser().getUserNum();
		User user = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new EntityNotFoundException("회원 정보가 존재하지 않습니다."));

		if(submit.getSubmitType().equals("휴가 신청서")){
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(submit.getContents());

			String vacationType = "";
			vacationType = rootNode.get("휴가종류").asText();
			// 병가면 병가 일수에서 차감
			if(vacationType.equals("병가")){

			}
			else {
				double totalVacationDays = rootNode.get("총휴가일수").asDouble();
				user.updateAnnualLeave(totalVacationDays);
			}
			user.updateAnnualLeave(getVacationDate(submit));
		}
	}

	// 총휴가일 수 계산
	public double getVacationDate(Submit submit){
		double totalVacationDays = 0;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(submit.getContents());
			totalVacationDays = rootNode.get("총휴가일수").asDouble();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return totalVacationDays;
	}



	// 결재 라인 조회
	public List<SubmitLineResDto> getSubmitLines(Long submitId){
		Submit submit = submitRepository.findById(submitId)
			.orElseThrow(() -> new EntityNotFoundException("결재 정보가 존재하지 않습니다."));

		List<SubmitLineResDto> dtos = new ArrayList<>();
		for(SubmitLine submitLine : submit.getSubmitLines()){
			User user = userRepository.findByUserNum(submitLine.getUserNum())
				.orElseThrow(() -> new EntityNotFoundException("회원정보가 존재하지 않습니다."));

			Position position = positionRepository.findById(user.getPosition().getId())
				.orElseThrow(() -> new EntityNotFoundException("직급 정보가 존재하지 않습니다."));
			dtos.add(submitLine.fromEntity(user, position));

		}

		return dtos;
	}

}
