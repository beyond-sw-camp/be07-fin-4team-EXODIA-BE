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
import jakarta.persistence.EntityManager; // 추가된 부분
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
	private final EntityManager entityManager;

	@Autowired
	public CommentService(CommentRepository commentRepository, BoardRepository boardRepository,
						  UserRepository userRepository, QnARepository qnARepository,
						  CommentDocRepository commentDocRepository, DocumentRepository documentRepository, EntityManager entityManager) { // 수정된 부분
		this.commentRepository = commentRepository;
		this.boardRepository = boardRepository;
		this.userRepository = userRepository;
		this.qnARepository = qnARepository;
		this.commentDocRepository = commentDocRepository;
		this.documentRepository = documentRepository;
		this.entityManager = entityManager;
	}

	@Transactional
	public Comment saveComment(CommentSaveReqDto dto) {
		User user = userRepository.findByUserNum(dto.getUserNum())
				.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
		if (dto.getDelYn() == null) {
			dto.setDelYn(DelYN.N); // 기본값 설정
		}
		Comment savedComment;
		if (dto.getBoard_id() != null) {
			Board board = boardRepository.findById(dto.getBoard_id())
					.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));
			savedComment = commentRepository.save(dto.BoardToEntity(user, board, dto.getUserNum()));
		} else if (dto.getQuestion_id() != null) {
			QnA qna = qnARepository.findById(dto.getQuestion_id())
					.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 질문입니다."));
			savedComment = commentRepository.save(dto.QnaToEntity(user, qna, dto.getUserNum()));
		} else {
			throw new IllegalArgumentException("댓글이 달릴 게시글이나 질문 ID가 필요합니다.");
		}
		return savedComment;
	}

	public List<CommentDetailDto> getCommentsByBoardId(Long BoardId) {
		List<Comment> comments = commentRepository.findByBoardIdAndDelYn(BoardId, DelYN.N);
		return comments.stream()
				.map(comment -> CommentDetailDto.builder()
						.id(comment.getId())
						.content(comment.getContent())
						.userNum(comment.getUser().getUserNum())
						.name(comment.getUser().getName())
						.createdAt(comment.getCreatedAt())
						.build())
				.collect(Collectors.toList());
	}

	@Transactional
	public Comment commentUpdate(Long id, CommentUpdateDto dto) {
		Comment comment = commentRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 댓글입니다."));
		if (!comment.getUser().getUserNum().equals(dto.getUserNum())) {
			throw new SecurityException("작성자 본인만 댓글을 수정할 수 있습니다.");
		}
		comment.setContent(dto.getContent());
		comment.updateTimestamp();
		return commentRepository.save(comment);
	}

	@Transactional
	public void commentDelete(Long id, String userNum) {
		Comment comment = commentRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 댓글입니다."));

		if (!comment.getUser().getUserNum().equals(userNum)) {
			throw new SecurityException("작성자 본인만 댓글을 삭제할 수 있습니다.");
		}


		commentRepository.markAsDeleted(id);
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
			.orElseThrow(() -> new EntityNotFoundException("파일 정보가 존재하지 않습니다."));

		List<CommentDoc> commentDocs =  commentDocRepository.findByDocumentOrderByCreatedAtDesc(document);
		return commentDocs.stream()
			.map(CommentDoc::fromEntity)
			.collect(Collectors.toList());
	}
}
