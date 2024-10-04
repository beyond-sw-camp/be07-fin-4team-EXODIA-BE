package com.example.exodia.comment.controller;

import com.example.exodia.comment.domain.Comment;
import com.example.exodia.comment.dto.CommentDetailDto;
import com.example.exodia.comment.dto.CommentSaveReqDto;
import com.example.exodia.comment.dto.CommentUpdateDto;
import com.example.exodia.comment.service.CommentService;
import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("comment")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 새로운 댓글을 생성합니다.
     * @param dto - 클라이언트에서 전달된 댓글 정보를 담고 있는 객체
     * @return ResponseEntity는 HTTP 상태 코드와 응답 데이터를 포함하여 클라이언트에 반환
     */
    @PostMapping("/create")
    public ResponseEntity<?> createComment(@RequestBody CommentSaveReqDto dto) {
        try {
            // 서비스 객체를 이용하여 새로운 댓글을 저장합니다.
            commentService.saveComment(dto);
            CommonResDto commonResDto;

            // 댓글이 특정 게시물에 등록되었는지 확인하고 응답 메시지를 설정
            if (dto.getBoard_id() != null) {
                // 댓글이 성공적으로 등록되었다는 응답 메시지와 함께 게시물 ID를 반환
                commonResDto = new CommonResDto(HttpStatus.CREATED, "댓글이 성공적으로 등록되었습니다.", dto.getBoard_id());
                return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
            } else {
                // 댓글 등록 시 필수 데이터가 누락되었을 때 발생하는 예외 처리
                throw new IllegalArgumentException("게시물 ID 또는 QnA ID가 제공되어야 합니다.");
            }
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            // IllegalArgumentException: 잘못된 파라미터가 전달된 경우
            // EntityNotFoundException: 데이터베이스에서 해당 게시물이나 QnA를 찾을 수 없는 경우
            e.printStackTrace();
            // 에러 메시지를 담은 응답 데이터를 반환합니다. 400 상태 코드
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 특정 게시물의 댓글 목록을 조회
     * @return 조회된 댓글 목록을 포함한 응답 데이터
     */
    @GetMapping("/board/{boardId}")
    public ResponseEntity<?> getCommentsByBoardId(@PathVariable Long boardId) {
        try {
            // 서비스 객체를 이용하여 특정 게시물의 댓글 목록을 조회합니다.
            List<CommentDetailDto> comments = commentService.getCommentsByBoardId(boardId);
            // 성공적인 조회 응답을 생성하여 반환합니다.
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "댓글 목록 조회 성공", comments);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            // 게시물을 찾을 수 없는 경우 발생하는 예외 처리
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
            return new ResponseEntity<>(commonErrorDto, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            // 그 외 모든 예외에 대한 처리
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 목록 조회에 실패했습니다.");
            return new ResponseEntity<>(commonErrorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 QnA의 댓글 목록을 조회
     * @return 조회된 댓글 목록을 포함한 응답 데이터
     */
    @GetMapping("/qna/{qnaId}")
    public ResponseEntity<?> getCommentsByQnaId(@PathVariable Long qnaId) {
        try {
            // 서비스 객체를 이용하여 특정 QnA의 댓글 목록을 조회합니다.
            List<CommentDetailDto> comments = commentService.getCommentsByBoardId(qnaId);
            // 성공적인 조회 응답을 생성하여 반환합니다.
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "댓글 목록 조회 성공", comments);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            // QnA를 찾을 수 없는 경우 발생하는 예외 처리
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
            return new ResponseEntity<>(commonErrorDto, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            // 그 외 모든 예외에 대한 처리
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 목록 조회에 실패했습니다.");
            return new ResponseEntity<>(commonErrorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 댓글을 수정합니다.
     * @param id - 수정할 댓글 ID
     * @param dto - 수정할 내용이 담긴 DTO 객체
     * @return 수정된 댓글의 ID와 상태 메시지를 포함한 응답 데이터입니다.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateComment(@PathVariable Long id, @RequestBody CommentUpdateDto dto) {
        try {
            commentService.commentUpdate(id, dto);
            return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");
        } catch (SecurityException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * 특정 댓글을 삭제합니다.
     * @return 삭제된 댓글의 ID를 포함한 응답 데이터입니다.
     */
    @GetMapping("/delete/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, @RequestParam("userNum") String userNum) {
        try {
            commentService.commentDelete(id, userNum);
            return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
        } catch (SecurityException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
