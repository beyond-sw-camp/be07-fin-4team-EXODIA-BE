package com.example.exodia.submit.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.document.dto.DocDetailResDto;
import com.example.exodia.submit.domain.Submit;
import com.example.exodia.submit.domain.SubmitLine;
import com.example.exodia.submit.dto.SubmitDetResDto;
import com.example.exodia.submit.dto.SubmitSaveReqDto;
import com.example.exodia.submit.dto.SubmitStatusUpdateDto;
import com.example.exodia.submit.repository.SubmitRepository;
import com.example.exodia.submit.repository.SubmitTypeRepository;
import com.example.exodia.submit.service.SubmitService;

@RestController
@RequestMapping("/submit")
public class SubmitController {

	private final SubmitService submitService;
	private final SubmitRepository submitRepository;

	@Autowired
	public SubmitController(SubmitService submitService, SubmitRepository submitRepository) {
		this.submitService = submitService;
		this.submitRepository = submitRepository;
	}

	// 	결재 요청
	@PostMapping("/create")
	public ResponseEntity<?> createSubmit(@RequestBody SubmitSaveReqDto submitSaveReqDto) {
		try {
			Submit submit = submitService.createSubmit(submitSaveReqDto);
			return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "결재 요청 성공", submit.getId()));
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
			return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	// 결재 상태 변경
	@PostMapping("/update")
	public ResponseEntity<?> updateSubmit(@RequestBody SubmitStatusUpdateDto submitStatusUpdateDto) {
		try {
			List<SubmitLine> submits = submitService.updateSubmit(submitStatusUpdateDto);
			return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "결재 상태 변경 성공", submits.size()));
		}catch(EntityNotFoundException e) {
			e.printStackTrace();
			return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);

		}
	}

	// 결재 타입 리스트 전체 조회
	@GetMapping("/type/list")
	public ResponseEntity<?> findAll() {
		List<?> submitTypes = submitService.getTypeList();
		return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "결재 타입 리스트 조회 성공", submitTypes));
	}

	// 나에게 요청 들어온 결재 리스트 조회
	@GetMapping("/list")
	public ResponseEntity<?> findReqSubmits() {
		List<?> submitTypes = submitService.getSubmitList();
		return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "나에게 요청 된 결재 리스트 조회 성공", submitTypes));
	}

	// 내가 요청한 결재 리스트 조회
	@GetMapping("/list/my")
	public ResponseEntity<?> findMySubmits() {
		List<?> submitTypes = submitService.getMySubmitList();
		return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "내가 요청한 결재 리스트 조회 성공", submitTypes));
	}


	// 결재 상세조회
	@GetMapping("/detail/{id}")
	public ResponseEntity<?> detailSubmit(@PathVariable Long id) {
		SubmitDetResDto submitDetail = submitService.getSubmitDetail(id);
		return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "결재 정보 조회 성공", submitDetail));
	}

	// 결재 취소 -> 자신의 글에 대해서만
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteSubmit(@PathVariable Long id) {
		submitService.deleteSubmit(id);
		return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "결재 취소 성공", null ));
	}





}
