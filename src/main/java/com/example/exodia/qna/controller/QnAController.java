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

    /**
     * 새로운 QnA 질문을 생성하는 메서드
     * @param dto - 클라이언트에서 전달된 질문 정보가 담긴 객체
     * @param files - 첨부 파일 목록 (선택적)
     * @param department - 질문을 등록할 부서 정보
     * @return 생성된 질문의 ID를 포함한 응답 데이터 반환
     */
    @PostMapping("/create")
    public ResponseEntity<?> createQuestion(QnASaveReqDto dto, @RequestPart(value = "files", required = false) List<MultipartFile> files, Department department) {
        try {
            // 서비스 객체를 이용하여 새로운 질문을 저장함
            QnA qna = qnAService.createQuestion(dto, files, department);
            // 질문이 성공적으로 생성되었다는 응답 데이터를 생성함
            CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "질문이 성공적으로 등록되었습니다.", qna.getId());
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // 잘못된 입력 값이 전달된 경우 발생하는 예외 처리
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 모든 QnA 질문 목록을 조회하는 메서드
     * @param pageable - 페이징 정보를 포함한 객체
     * @param searchCategory - 검색 카테고리 (선택적)
     * @param searchQuery - 검색어 (선택적)
     * @param departmentId - 부서 ID (선택적)
     * @return 조회된 질문 목록 반환
     */// '/qna/list' 경로로 GET 요청이 들어오면 이 메서드가 실행됨
    public ResponseEntity<?> getAllQuestions(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String searchCategory,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Long departmentId) {

        Page<QnAListResDto> qnaList; // 조회된 질문 목록을 저장할 변수 선언

        // 부서 ID가 있을 경우, 해당 부서의 질문 목록을 조회함
        if (departmentId != null) {
            qnaList = qnAService.qnaListByGroup(departmentId, pageable);
        } else {
            // 부서 ID가 없으면, 검색 카테고리와 검색어를 이용하여 질문 목록을 조회함
            qnaList = qnAService.qnaListWithSearch(pageable, searchCategory, searchQuery);
        }

        // 조회된 질문 목록을 포함한 응답 데이터를 생성함
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "질문 목록을 조회합니다.", qnaList);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    /**
     * 특정 QnA 질문의 상세 정보를 조회하는 메서드
     * @param id - 조회할 질문의 고유 ID
     * @return 조회된 질문의 상세 정보 반환
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getQuestionDetail(@PathVariable Long id) {
        try {
            // 서비스 객체를 이용하여 질문의 상세 정보를 조회함
            QnADetailDto questionDetail = qnAService.getQuestionDetail(id);
            // 성공적인 조회 응답을 생성하여 반환함
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "질문 상세 정보를 조회합니다.", questionDetail);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            // 질문을 찾을 수 없는 경우 발생하는 예외 처리
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 특정 QnA 질문에 답변을 등록하는 메서드
     * @param id - 답변할 질문의 고유 ID
     * @param dto - 답변 정보가 담긴 객체
     * @param files - 첨부 파일 목록 (선택적)
     * @return 답변이 성공적으로 등록되었다는 응답 데이터 반환
     */
    @PostMapping("/answer/{id}")
    public ResponseEntity<?> answerQuestion(@PathVariable Long id, QnAAnswerReqDto dto,
                                            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            // 서비스 객체를 이용하여 질문에 대한 답변을 저장함
            qnAService.answerQuestion(id, dto, files);
            // 답변이 성공적으로 등록되었다는 응답 데이터를 생성하여 반환함
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "질문에 대한 답변이 성공적으로 등록되었습니다.", id);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (SecurityException | EntityNotFoundException e) {
            // 권한이 없는 경우 또는 질문을 찾을 수 없는 경우 발생하는 예외 처리
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 특정 QnA 질문을 수정하는 메서드
     * @param id - 수정할 질문의 고유 ID
     * @param dto - 수정할 질문 정보가 담긴 객체
     * @param files - 첨부 파일 목록 (선택적)
     * @return 수정된 질문의 ID를 포함한 응답 데이터 반환
     */
    @PostMapping("/update/question/{id}")
    public ResponseEntity<?> qnaQUpdate(@PathVariable Long id, QnAQtoUpdateDto dto,
                                        @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            // 서비스 객체를 이용하여 질문을 수정함
            qnAService.QnAQUpdate(id, dto, files);
            // 질문이 성공적으로 수정되었다는 응답 데이터를 생성하여 반환함
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "질문이 성공적으로 업데이트되었습니다.", id);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            // 질문을 찾을 수 없는 경우 발생하는 예외 처리
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 특정 QnA 답변을 수정하는 메서드
     * @param id - 수정할 답변의 고유 ID
     * @param dto - 수정할 답변 정보가 담긴 객체
     * @param files - 첨부 파일 목록 (선택적)
     * @return 수정된 답변의 ID를 포함한 응답 데이터 반환
     */
    @PostMapping("/update/answer/{id}")
    public ResponseEntity<?> qnaAUpdate(@PathVariable Long id, QnAAtoUpdateDto dto,
                                        @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            // 서비스 객체를 이용하여 답변을 수정함
            qnAService.QnAAUpdate(id, dto, files);
            // 답변이 성공적으로 수정되었다는 응답 데이터를 생성하여 반환함
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "답변이 성공적으로 업데이트되었습니다.", id);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            // 답변을 찾을 수 없는 경우 발생하는 예외 처리
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 특정 QnA 질문을 삭제하는 메서드
     * @param id - 삭제할 질문의 고유 ID
     * @return 삭제된 질문의 ID를 포함한 응답 데이터 반환
     */
    @GetMapping("/delete/{id}")
    public ResponseEntity<?> qnaDelete(@PathVariable Long id) {
        try {
            // 서비스 객체를 이용하여 질문을 삭제함
            qnAService.qnaDelete(id);
            // 질문이 성공적으로 삭제되었다는 응답 데이터를 생성하여 반환함
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "질문이 성공적으로 삭제되었습니다.", id);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            // 질문을 찾을 수 없는 경우 발생하는 예외 처리
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 특정 부서의 QnA 질문 목록을 조회하는 메서드
     * @param departmentId - 조회할 부서의 고유 ID
     * @param pageable - 페이징 정보를 포함한 객체
     * @return 조회된 QnA 질문 목록 반환
     */
    @GetMapping("/department/{departmentId}/list")
    public ResponseEntity<?> lectureGroupBySubjectListView(@PathVariable("departmentId") Long departmentId,
                                                           @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        // 서비스 객체를 이용하여 해당 부서의 QnA 질문 목록을 조회함
        Page<QnAListResDto> qnAListResDtos = qnAService.qnaListByGroup(departmentId, pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "해당 부서의 QnA 목록을 조회합니다.", qnAListResDtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    /**
     * 현재 사용자 본인이 작성한 질문 목록을 조회하는 메서드
     * @return 조회된 질문 목록 반환
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyQuestions() {
        // 서비스 객체를 이용하여 현재 사용자가 작성한 질문 목록을 조회함
        List<QnAListResDto> qnaList = qnAService.getUserQnAs();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "사용자 작성 질문 목록을 조회합니다.", qnaList);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}


