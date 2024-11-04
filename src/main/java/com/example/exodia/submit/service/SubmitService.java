package com.example.exodia.submit.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.exodia.board.domain.Board;
import com.example.exodia.board.domain.Category;
import com.example.exodia.board.dto.BoardSaveReqDto;
import com.example.exodia.board.repository.BoardRepository;
import com.example.exodia.board.service.BoardAutoUploadService;
import com.example.exodia.common.service.KafkaProducer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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
import com.example.exodia.user.domain.NowStatus;
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
	private ThreadPoolTaskScheduler taskScheduler;

	public SubmitService(SubmitRepository submitRepository, UserRepository userRepository,
		PositionRepository positionRepository, SubmitLineRepository submitLineRepository,
		SubmitTypeRepository submitTypeRepository, KafkaProducer kafkaProducer,
		DepartmentRepository departmentRepository, BoardRepository boardRepository,
		BoardAutoUploadService boardAutoUploadService, ThreadPoolTaskScheduler taskScheduler) {
		this.submitRepository = submitRepository;
		this.userRepository = userRepository;
		this.positionRepository = positionRepository;
		this.submitLineRepository = submitLineRepository;
		this.kafkaProducer = kafkaProducer;
		this.submitTypeRepository = submitTypeRepository;
		this.departmentRepository = departmentRepository;
		this.boardRepository = boardRepository;
		this.boardAutoUploadService = boardAutoUploadService;
		this.taskScheduler = taskScheduler;
	}

	//    결재라인 등록
	@Transactional
	public Submit createSubmit(SubmitSaveReqDto dto) throws IOException {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

		User submitUser = userRepository.findByUserNum(userNum)
			.orElseThrow(() -> new EntityNotFoundException("회원정보가 존재하지 않습니다."));

		String receiverUserNum = null;
		if (dto.getSubmitUserDtos() == null) {
			throw new IOException("결재 라인을 등록하십시오.");
		}

		Submit submit = dto.toEntity(submitUser);
		SubmitLine submitLine = SubmitLine.builder().build();

		for (SubmitSaveReqDto.SubmitUserDto userDto : dto.getSubmitUserDtos()) {
			Position position = positionRepository.findById(userDto.getPosition())
				.orElseThrow(() -> new EntityNotFoundException("직급 정보가 존재하지 않습니다."));

			User user = userRepository.findByNameAndPosition(userDto.getUserName(), position)
				.orElseThrow(() -> new EntityNotFoundException("회원정보가 존재하지 않습니다."));

			submitLine = dto.toLineEntity(user);
			if (receiverUserNum == null) {
				receiverUserNum = submitLine.getUserNum();
			}

			submit.getSubmitLines().add(submitLine);
			submitLine.updateSubmit(submit);
		}

		submitRepository.save(submit);
		submitLineRepository.save(submitLine);

		kafkaProducer.sendSubmitNotification(
				"submit-events",
				dto.getSubmitUserDtos().get(0).getUserName(),
				receiverUserNum,
				LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM.dd")),
				submit.getId()
		);
		return submit;
	}

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

					// 반려 시 kafka 알림
					kafkaProducer.revokeSubmitNotification("submit-events", submit.getUserNum(), submit.getId());

					break;
				} else if (dto.getStatus() == SubmitStatus.승인) {
					submitLine.updateStatus(SubmitStatus.승인);

					// 최상단 결재자인 경우
					if (i == submitLines.size() - 1) {
						submit.updateStatus(SubmitStatus.승인, null);

						// kafka 결재 승인 알림(최종 찐 승인)
						kafkaProducer.allSubmitNotification("submit-events", submit.getUserNum(), submit.getId());

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

						// 알림(1->2 2->3)
						kafkaProducer.sendSubmitNotification("submit-events", nextUser.getName(),
							nextUser.getUserNum(),
								LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM.dd")), submit.getId());
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
	@Transactional
	public List<?> getTypeList() {
		List<SubmitType> types = submitTypeRepository.findAll();
		return types.stream()
			.map(SubmitType::getTypeName)
			.collect(Collectors.toList());
	}

	// 나에게 요청 들어온 결재 리스트 조회
	@Transactional
	public List<?> getSubmitList() {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

		List<SubmitLine> submitLines = submitLineRepository.findAllByUserNumOrderByCreatedAtDesc(userNum);
		List<SubmitListResDto> submitList = new ArrayList<>();

		for (SubmitLine submitLine : submitLines) {
			User user = submitLine.getSubmit().getUser();
			Long submitLineId = submitLine.getId();

			Optional<SubmitLine> before = submitLineRepository.findById(submitLineId - 1L);

			if (!before.isEmpty()) {
					SubmitLine beforeSubmit = before.get();

				// 유일한 경우가 아니고 이전 결재가 승인 상태라면?
				if (beforeSubmit.getSubmit() == submitLine.getSubmit()) {
					if(beforeSubmit.getSubmitStatus() == SubmitStatus.승인){
						submitList.add(new SubmitListResDto().fromLineEntity(user, submitLine));
					}
				}else{
					// 있긴한데 다른 결재인 경우
					submitList.add(new SubmitListResDto().fromLineEntity(user, submitLine));
				}
			}else {
				// 이전 결재가 없는 경우
				submitList.add(new SubmitListResDto().fromLineEntity(user, submitLine));
			}
		}
		return submitList;
	}

	// 내가 요청한 결재 리스트 조회
	public Page<SubmitListResDto> getMySubmitList(Pageable pageable) {
		String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNum(userNum)
				.orElseThrow(() -> new EntityNotFoundException("회원 정보가 존재하지 않습니다."));

		Page<Submit> submits = submitRepository.findAllByUserOrderByCreatedAtDesc(user, pageable);

		return submits.map(submit -> new SubmitListResDto().fromEntity(submit));
	}
	// 결재 상세 조회
	@Transactional
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
			for(SubmitLine submitLine : submit.getSubmitLines()){
				submitLine.softDelete();
			}
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

		if (submit.getSubmitType().equals("휴가 신청서")) {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(submit.getContents());

			String vacationType = "";
			vacationType = rootNode.get("휴가종류").asText();
			double totalVacationDays = rootNode.get("총휴가일수").asDouble();

			// 병가면 병가 일수에서 차감
			if (vacationType.equals("병가")) {
				user.updateSickDay(totalVacationDays);
			} else {
				user.updateAnnualLeave(totalVacationDays);
			}
			user.updateAnnualLeave(getVacationDate(submit));
			// 날짜에 스케쥴링을 통해 상태 변경
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDateTime startDate = LocalDate.parse(rootNode.get("휴가시작일").asText(), formatter).atStartOfDay();
			LocalDateTime endDate = LocalDate.parse(rootNode.get("휴가종료일").asText(), formatter).atStartOfDay();

			scheduleVacationStatus(startDate,  endDate);
		}
	}

	// 총휴가일 수 계산
	public double getVacationDate(Submit submit) {
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
	@Transactional
	public List<SubmitLineResDto> getSubmitLines(Long submitId) {
		Submit submit = submitRepository.findById(submitId)
			.orElseThrow(() -> new EntityNotFoundException("결재 정보가 존재하지 않습니다."));

		List<SubmitLineResDto> dtos = new ArrayList<>();
		for (SubmitLine submitLine : submit.getSubmitLines()) {
			User user = userRepository.findByUserNum(submitLine.getUserNum())
				.orElseThrow(() -> new EntityNotFoundException("회원정보가 존재하지 않습니다."));

			Position position = positionRepository.findById(user.getPosition().getId())
				.orElseThrow(() -> new EntityNotFoundException("직급 정보가 존재하지 않습니다."));
			dtos.add(submitLine.fromEntity(user, position));
		}
		return dtos;
	}


	/* 스케줄링을 통한 상태 변경 */
	private void scheduleVacationStatus(LocalDateTime startDate, LocalDateTime endDate) {
		taskScheduler.schedule(() -> updateVacationStatus(NowStatus.휴가),
			Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant()));


		taskScheduler.schedule(() -> updateVacationStatus(NowStatus.근무전),
			Date.from(endDate.atZone(ZoneId.systemDefault()).toInstant()));
	}

	/* 상태 업데이트 메서드 */
	@Transactional
	public void updateVacationStatus(NowStatus status) {
		// 승인된 휴가신청서 확인
		List<Submit> submits = submitRepository.findAllBySubmitStatusAndSubmitType(SubmitStatus.승인, "휴가신청서");
		submits.forEach(submit -> {
			User user = submit.getUser();
			user.setN_status(NowStatus.휴가);
			userRepository.save(user);
		});
	}

	public Page<SubmitListResDto> filterSubmit(String filterType, String  filterValue, Pageable pageable) {
		Page<Submit> submits = Page.empty();
		if(filterType.equals("submitStatus")){
			SubmitStatus status = SubmitStatus.valueOf(filterValue);
			submits = submitRepository.findBySubmitStatusOrderByCreatedAtDesc(status, pageable);
		}else if(filterType.equals("submitType")){
			SubmitType type = submitTypeRepository.findByTypeName(filterValue);
			submits = submitRepository.findBySubmitTypeOrderByCreatedAtDesc(filterValue, pageable);
		}
		return submits.map(submit -> new SubmitListResDto().fromEntity(submit));

	}
}
