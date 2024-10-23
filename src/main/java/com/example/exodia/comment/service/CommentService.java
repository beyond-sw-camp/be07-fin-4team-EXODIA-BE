package com.example.exodia.comment.service;

import com.example.exodia.board.domain.Board;
import com.example.exodia.board.repository.BoardRepository;
import com.example.exodia.comment.domain.Comment;
import com.example.exodia.comment.domain.CommentDoc;
import com.example.exodia.comment.dto.CommentDetailDto;
import com.example.exodia.comment.dto.CommentSaveReqDto;
import com.example.exodia.comment.dto.CommentUpdateDto;
import com.example.exodia.comment.dto.document.CommentDocListResDto;
import com.example.exodia.comment.dto.document.CommentDocSaveReqDto;
import com.example.exodia.comment.repository.CommentDocRepository;
import com.example.exodia.comment.repository.CommentRepository;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.document.domain.Document;
import com.example.exodia.document.repository.DocumentRepository;
import com.example.exodia.qna.domain.QnA;
import com.example.exodia.qna.repository.QnARepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

	private final CommentRepository commentRepository;
	private final BoardRepository boardRepository;
	private final UserRepository userRepository;
	private final QnARepository qnARepository;
	private final CommentDocRepository commentDocRepository;
	private final DocumentRepository documentRepository;

	@Autowired
	public CommentService(CommentRepository commentRepository, BoardRepository boardRepository,
		UserRepository userRepository, QnARepository qnARepository,
		CommentDocRepository commentDocRepository, DocumentRepository documentRepository) {
		this.commentRepository = commentRepository;
		this.boardRepository = boardRepository;
		this.userRepository = userRepository;
		this.qnARepository = qnARepository;
		this.commentDocRepository = commentDocRepository;
		this.documentRepository = documentRepository;
	}

	@Transactional
	public Comment saveComment(CommentSaveReqDto dto) {

		// 현재 인증된 사용자 정보를 가져옴
		User user = userRepository.findByUserNum(dto.getUserNum())
			.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다.")); // 사용자 정보가 없을 경우 예외 발생

		// 댓글 삭제 여부(delYn) 값이 설정되지 않은 경우 기본값 'N'(삭제되지 않음)으로 설정
		if (dto.getDelYn() == null) {
			dto.setDelYn(DelYN.N); // delYn 값이 null이면 N으로 설정 (삭제되지 않은 상태)
		}

		Comment savedComment; // 저장될 댓글 객체 선언

		// 댓글이 달릴 게시물(Board) 또는 질문(QnA)이 존재하는지 확인
		if (dto.getBoard_id() != null) {
			// `board_id`가 있을 경우 Board 엔티티를 조회
			Board board = boardRepository.findById(dto.getBoard_id())
				.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다.")); // 게시물이 없을 경우 예외 발생

			// Board 엔티티를 기반으로 Comment 생성 및 저장
			savedComment = commentRepository.save(dto.BoardToEntity(user, board, dto.getUserNum()));
		} else if (dto.getQuestion_id() != null) {
			// `question_id`가 있을 경우 QnA 엔티티를 조회
			QnA qna = qnARepository.findById(dto.getQuestion_id())
				.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 질문입니다.")); // 질문이 없을 경우 예외 발생

			// QnA 엔티티를 기반으로 Comment 생성 및 저장
			savedComment = commentRepository.save(dto.QnaToEntity(user, qna, dto.getUserNum()));
		} else {
			// 댓글을 달 게시물이나 질문이 제공되지 않은 경우 예외 발생
			throw new IllegalArgumentException("댓글이 달릴 게시글이나 질문 ID가 필요합니다.");
		}

		// 저장된 댓글 객체 반환
		return savedComment;
	}


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


	@Transactional
	public Comment commentUpdate(Long id, CommentUpdateDto dto) {
		// 수정할 댓글을 ID로 조회
		Comment comment = commentRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 댓글입니다.")); // 댓글이 없을 경우 예외 발생

		// 요청에서 받은 userNum을 사용하여 권한 확인
		if (!comment.getUser().getUserNum().equals(dto.getUserNum())) {
			throw new SecurityException("작성자 본인만 댓글을 수정할 수 있습니다.");
		}

		// 댓글 내용 업데이트
		comment.setContent(dto.getContent());
		comment.updateTimestamp(); // updatedAt 수동 갱신

		// 업데이트된 댓글 객체 반환
		return commentRepository.save(comment);
	}

	@Transactional
	public void commentDelete(Long id, String userNum) {
		// 삭제할 댓글을 ID로 조회
		Comment comment = commentRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 댓글입니다.")); // 댓글이 없을 경우 예외 발생

		// 요청에서 받은 userNum을 사용하여 권한 확인
		if (!comment.getUser().getUserNum().equals(userNum)) {
			throw new SecurityException("작성자 본인만 댓글을 삭제할 수 있습니다.");
		}

		// 댓글 논리 삭제 처리 (delYn과 deletedAt 설정)
		comment.softDelete();
		commentRepository.save(comment);
	}

	// 문서 댓글 작성
	@Transactional
	public CommentDoc createDocComment(CommentDocSaveReqDto commentDocSaveReqDto) {
		User user = userRepository.findByUserNum(commentDocSaveReqDto.getUserNum())
			.orElseThrow(() -> new EntityNotFoundException("회원정보가 존재하지 않습니다."));

		Document document = documentRepository.findById(commentDocSaveReqDto.getDocumentId())
			.orElseThrow(() -> new EntityNotFoundException("문서 정보가 존재하지 않습니다."));

		return commentDocRepository.save(commentDocSaveReqDto.toEntity(user, document));
	}

	// 문서 댓글 삭제
	@Transactional
	public void deleteDocComment(Long id) {
		CommentDoc commentDoc = commentDocRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("문서/댓글 정보가 존재하지 않습니다."));

		// 댓글 논리 삭제 처리 (deletedAt과 delYn 설정)
		commentDoc.softDelete();
		commentDocRepository.save(commentDoc);
	}
	// 문서별 댓글 조회
	public List<CommentDocListResDto> getDocCommentList(Long id) {
		Document document = documentRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("문서 정보가 존재하지 않습니다."));

		List<CommentDoc> commentDocs =  commentDocRepository.findByDocumentOrderByCreatedAtDesc(document);
		return commentDocs.stream()
			.map(CommentDoc::fromEntity)
			.collect(Collectors.toList());
	}
}
