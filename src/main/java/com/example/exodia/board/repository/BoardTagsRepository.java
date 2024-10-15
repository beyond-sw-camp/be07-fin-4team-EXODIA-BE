package com.example.exodia.board.repository;

import com.example.exodia.board.domain.Tags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardTagsRepository extends JpaRepository<Tags, Long> {

    // 특정 게시물에 해당하는 모든 태그를 삭제
    void deleteAllByBoardId(Long boardId);

    // 특정 게시물에 해당하는 모든 태그를 조회
    List<Tags> findAllByBoardId(Long boardId);

    // 특정 태그명으로 태그를 검색
    List<Tags> findByTagContainingIgnoreCase(String tag);
}
