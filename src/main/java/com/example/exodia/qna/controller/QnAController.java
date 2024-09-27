package com.example.exodia.qna.controller;


import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.department.domain.Department;
import com.example.exodia.qna.domain.QnA;
import com.example.exodia.qna.dto.*;
import com.example.exodia.qna.service.QnAService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@RestController
@RequestMapping("qna")
public class QnAController {

    private final QnAService qnAService;

    @Autowired
    public QnAController(QnAService qnAService) {
        this.qnAService = qnAService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createQuestion(QnASaveReqDto dto, @RequestPart(value = "files", required = false) List<MultipartFile> files, Department department) {
        try {
            QnA qna = qnAService.createQuestion(dto, files, department);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "질문이 성공적으로 등록되었습니다.", qna.getId());
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllQuestions(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String searchCategory,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Long departmentId) {

        Page<QnAListResDto> qnaList;

        if (departmentId != null) {
            qnaList = qnAService.qnaListByGroup(departmentId, pageable);
        } else {
            qnaList = qnAService.qnaListWithSearch(pageable, searchCategory, searchQuery);
        }

        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "질문 목록을 조회합니다.", qnaList);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getQuestionDetail(@PathVariable Long id) {
        try {
            QnADetailDto questionDetail = qnAService.getQuestionDetail(id);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "질문 상세 정보를 조회합니다.", questionDetail);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/answer/{id}")
    public ResponseEntity<?> answerQuestion(@PathVariable Long id, QnAAnswerReqDto dto,
                                            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            qnAService.answerQuestion(id, dto, files);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "질문에 대한 답변이 성공적으로 등록되었습니다.", id);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (SecurityException | EntityNotFoundException e) {
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/update/question/{id}")
    public ResponseEntity<?> qnaQUpdate(@PathVariable Long id, QnAQtoUpdateDto dto,
                                        @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            qnAService.QnAQUpdate(id, dto, files);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "질문이 성공적으로 업데이트되었습니다.", id);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/update/answer/{id}")
    public ResponseEntity<?> qnaAUpdate(@PathVariable Long id, QnAAtoUpdateDto dto,
                                        @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            qnAService.QnAAUpdate(id, dto, files);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "답변이 성공적으로 업데이트되었습니다.", id);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/delete/{id}")
    public ResponseEntity<?> qnaDelete(@PathVariable Long id) {
        try {
            qnAService.qnaDelete(id);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "질문이 성공적으로 삭제되었습니다.", id);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/department/{departmentId}/list")
    public ResponseEntity<?> lectureGroupBySubjectListView(@PathVariable("departmentId") Long departmentId,
                                                           @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<QnAListResDto> qnAListResDtos = qnAService.qnaListByGroup(departmentId, pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "해당 부서의 QnA 목록을 조회합니다.", qnAListResDtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyQuestions() {
        List<QnAListResDto> qnaList = qnAService.getUserQnAs();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "사용자 작성 질문 목록을 조회합니다.", qnaList);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}

