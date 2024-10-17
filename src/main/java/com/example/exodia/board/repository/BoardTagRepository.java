package com.example.exodia.board.repository;

import com.example.exodia.board.domain.BoardTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BoardTagRepository extends JpaRepository<BoardTag, Long> {


    List<BoardTag> findByBoardId(Long boardId);

    @Modifying
    @Transactional
    @Query("DELETE FROM BoardTag bt WHERE bt.board.id = :boardId")
    void deleteByBoardId(Long boardId);
}
