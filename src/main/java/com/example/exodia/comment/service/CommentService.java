package com.example.exodia.comment.service;

import com.example.exodia.board.domain.Board;
import com.example.exodia.board.repository.BoardRepository;
import com.example.exodia.comment.domain.Comment;
import com.example.exodia.comment.dto.CommentDetailDto;
import com.example.exodia.comment.dto.CommentSaveReqDto;
import com.example.exodia.comment.repository.CommentRepository;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;


    @Autowired
    public CommentService(CommentRepository commentRepository, BoardRepository boardRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }


    /**
     * 새로운 댓글을 저장하는 메서드
     * @param dto - 클라이언트에서 전달된 댓글 정보를 담고 있는 객체
     * @return 저장된 댓글 객체(Comment)를 반환
     */
    public Comment saveComment(CommentSaveReqDto dto) {

        // 현재 인증된 사용자 정보를 가져옴
        User user = userRepository.findByUserNum(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다람.")); // 사용자 정보가 없을 경우 예외를 발생

        String userNum = user.getUserNum(); // 사용자의 사번 또는 식별자를 가져옵니다.

        // 댓글 삭제 여부(delYn) 값이 설정되지 않은 경우 기본값 'N'(삭제되지 않음)으로 설정
        if (dto.getDelYn() == null) {
            dto.setDelYn(DelYN.N); // delYn 값이 null이면 N으로 설정 (삭제되지 않은 상태)
        }

        Comment savedComment; // 저장될 댓글 객체 선언

        // 댓글이 달릴 게시물(Board)이 존재하는지 확인
        if (dto.getBoard_id() != null) {
            // 댓글이 달릴 게시물 ID로 게시물을 조회
            Board board = boardRepository.findById(dto.getBoard_id())
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다.")); // 게시물이 없을 경우 예외 발생

            // DTO에서 전달받은 데이터를 사용하여 새로운 댓글(Comment) 객체를 생성하고 저장
            savedComment = commentRepository.save(dto.BoardToEntity(user, board, userNum));
        } else {
            // 댓글을 달 게시물이 제공되지 않은 경우 예외 발생
            throw new IllegalArgumentException("댓글이 달릴 게시글 필요합니다.");
        }

        // 저장된 댓글 객체 반환
        return savedComment;
    }

    /**
     * 특정 게시물에 달린 댓글 리스트를 조회하는 메서드
     * @param BoardId - 댓글을 조회할 게시물 ID
     * @return 댓글 목록을 반환
     */
    public List<CommentDetailDto> getCommentsByBoardId(Long BoardId) {
        // 해당 게시물 ID와 삭제 여부(N) 조건으로 댓글 목록을 조회합니다.
        List<Comment> comments = commentRepository.findByBoardIdAndDelYn(BoardId, DelYN.N);

        // 조회된 댓글들을 DTO(CommentDetailDto) 형태로 변환하여 반환합니다.
        return comments.stream()
                .map(comment -> CommentDetailDto.builder()
                        .id(comment.getId()) // 댓글 ID
                        .content(comment.getContent()) // 댓글 내용
                        .userNum(comment.getUser().getUserNum()) // 댓글 작성자 사번 또는 사용자 식별자
                        .name(comment.getUser().getName()) // 댓글 작성자 이름
                        .createdAt(comment.getCreatedAt()) // 댓글 작성 시간
                        .build()) // DTO 객체 생성
                .collect(Collectors.toList()); // DTO 리스트로 변환하여 반환
    }

    /**
     * 특정 댓글을 삭제하는 메서드
     * @param id - 삭제할 댓글 ID
     * @return 삭제된 댓글 객체를 반환
     */
    @Transactional // 트랜잭션을 적용하여 데이터의 일관성을 보장
    public Comment commentDelete(Long id) {
        // 삭제할 댓글을 ID로 조회
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 댓글입니다.")); // 댓글이 없을 경우 예외 발생

        // 현재 사용자 ID(사번)를 가져오기
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

        // 댓글 작성자와 현재 사용자가 동일한지 확인
        if (!comment.getUser().getUserNum().equals(userNum)) {
            throw new SecurityException("작성자 본인만 댓글을 삭제할 수 있습니다.");
        }

        // 댓글의 삭제 상태를 'Y'로 업데이트
        comment.updateDelYN(DelYN.Y);

        // 삭제된 댓글 객체 반환
        return comment;
    }

}
