package com.example.exodia.board.repository;

import com.example.exodia.board.domain.BoardFile;
import com.example.exodia.qna.domain.QnA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardFileRepository extends JpaRepository<BoardFile, Long> {
    // Board와 관련된 모든 파일 삭제
    void deleteByBoardId(Long boardId);

    // Board와 관련된 모든 파일 조회
    List<BoardFile> findByBoardId(Long boardId);

    // QnA의 질문과 관련된 모든 파일 삭제 (질문자의 파일 삭제)
    void deleteByQuestion(QnA question);

    // QnA의 답변과 관련된 모든 파일 삭제 (답변자의 파일 삭제)
    void deleteByAnswer(QnA answer);

    // 파일 ID로 삭제
    void deleteById(Long fileId); // qnaId -> fileId로 이름 변경
}
