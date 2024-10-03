package com.example.exodia.board.controller;

import com.example.exodia.board.domain.Category;
import com.example.exodia.board.dto.*;
import com.example.exodia.board.service.BoardService;
import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    @Autowired
    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/create") // 게시물 작성 화면을 반환하는 메서드
    public String getCreateBoardPage() {
        return "/board/create";
    }

    /**
     * 새로운 게시물 작성 기능
     * @param dto - 사용자가 작성한 게시물 정보가 담긴 객체
     * @return HTTP 응답 본문과 상태 코드를 포함한 ResponseEntity 반환
     */
    @PostMapping("/create")
    public ResponseEntity<?> createBoard(@ModelAttribute BoardSaveReqDto dto) {
        try {
            // files 값이 null일 경우 빈 리스트로 처리
            List<MultipartFile> files = (dto.getFiles() != null) ? dto.getFiles() : Collections.emptyList();

            // 게시물 정보와 파일 정보 저장
            boardService.createBoard(dto, files);
            CommonResDto response = new CommonResDto(HttpStatus.CREATED, "게시물이 성공적으로 등록되었습니다.", null);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (SecurityException | EntityNotFoundException e) {
            e.printStackTrace();
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * 게시물 목록 조회
     * @param pageable - 페이징 정보와 정렬 방식을 담은 객체
     * @param searchQuery - 검색어
     * @param searchType - 검색 유형 (예: 제목, 내용 등)
     * @return 조회된 게시물 목록을 포함한 ResponseEntity 반환
     * ResponseEntity는 서버가 클라이언트에게 "응답을 어떻게 줄지"에 대한 설정을 할 수 있는 객체
     */
    @GetMapping("/list")
    public ResponseEntity<?> getBoardList(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "searchQuery", required = false) String searchQuery,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "category", required = false) Category category) {

        // 검색 조건과 카테고리를 기반으로 게시물 목록 조회
        Page<BoardListResDto> boardListResDto = boardService.BoardListWithSearch(pageable, searchType, searchQuery, category);
        CommonResDto response = new CommonResDto(HttpStatus.OK, "게시물 목록을 반환합니다.", boardListResDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * 특정 게시물의 상세 정보 조회
     * @param id - 조회할 게시물의 고유 ID
     * @return 게시물 상세 정보를 포함한 ResponseEntity 반환
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getBoardDetail(@PathVariable Long id) {
        try {
            BoardDetailDto boardDetail = boardService.BoardDetail(id);
            CommonResDto response = new CommonResDto(HttpStatus.OK, "게시물 상세 정보를 반환합니다.", boardDetail);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 게시물 수정 기능
     * @param id - 수정할 게시물의 고유 ID
     * @param dto - 수정할 게시물 정보가 담긴 객체
     * @return 수정된 게시물 정보를 포함한 ResponseEntity 반환
     */
    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateBoard(@PathVariable Long id, @ModelAttribute BoardUpdateDto dto) {
        try {
            boardService.updateBoard(id, dto, dto.getFiles());
            CommonResDto response = new CommonResDto(HttpStatus.OK, "게시물이 성공적으로 수정되었습니다.", id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 게시물 삭제 기능
     * @param id - 삭제할 게시물의 고유 ID
     * @return 삭제된 게시물의 ID를 포함한 ResponseEntity 반환
     */
    @GetMapping("/delete/{id}")
    public ResponseEntity<?> deleteBoard(@PathVariable Long id) {
        try {
            boardService.deleteBoard(id);
            CommonResDto response = new CommonResDto(HttpStatus.OK, "게시물이 성공적으로 삭제되었습니다.", id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 게시물 상단 고정 기능
     * @param id - 상단 고정할 게시물의 고유 ID
     * @param requestDto - 상단 고정 정보가 담긴 객체
     * @return 상단 고정/해제 결과를 포함한 ResponseEntity 반환
     */
    @PostMapping("/pin/{id}")
    public ResponseEntity<?> pinBoard(@PathVariable Long id, @RequestBody BoardPinReqDto requestDto) {
        try {
            boardService.pinBoard(id, requestDto.getUserId(), requestDto.getIsPinned());

            String message = requestDto.getIsPinned()
                    ? "게시물이 상단에 고정되었습니다."
                    : "게시물의 상단 고정이 해제되었습니다.";

            CommonResDto response = new CommonResDto(HttpStatus.OK, message, id);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (SecurityException e) {
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.FORBIDDEN, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }


}
