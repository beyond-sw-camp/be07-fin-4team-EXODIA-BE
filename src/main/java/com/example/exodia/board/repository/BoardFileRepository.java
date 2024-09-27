package com.example.exodia.board.repository;

import com.example.exodia.board.domain.BoardFile;
import com.example.exodia.qna.domain.QnA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardFileRepository extends JpaRepository<BoardFile, Long> {
    void deleteByBoardId(Long boardId);
    List<BoardFile> findByBoardId(Long boardId);

    // QnA와 관련된 모든 파일 삭제 (질문자의 파일)
    void deleteByQna(QnA qna);


    void deleteByQnaId(Long qnaId);
}
