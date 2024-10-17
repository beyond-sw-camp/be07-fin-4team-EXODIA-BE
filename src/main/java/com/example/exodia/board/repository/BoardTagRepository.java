package com.example.exodia.board.repository;

import com.example.exodia.board.domain.BoardTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BoardTagRepository extends JpaRepository<BoardTag, Long> {

    // 특정 boardId에 해당하는 BoardTag 목록 조회
    List<BoardTag> findByBoardId(Long boardId);

    // 특정 boardId에 해당하는 BoardTag 데이터 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM BoardTag bt WHERE bt.board.id = :boardId")
    void deleteByBoardId(Long boardId);

    // 태그 ID로 연관된 BoardTag 데이터 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM BoardTag bt WHERE bt.tags.id = :tagId")
    void deleteByTagId(Long tagId);
}
