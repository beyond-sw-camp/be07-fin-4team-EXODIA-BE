package com.example.exodia.board.repository;

import com.example.exodia.board.domain.Board;
import com.example.exodia.board.domain.Category;
import com.example.exodia.common.domain.DelYN;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 카테고리와 삭제 여부로만 필터링하여 검색
    Page<Board> findByCategoryAndDelYn(Category category, DelYN delYN, Pageable pageable);

    Optional<Board> findById(Long boardId);

    @Query("SELECT b FROM Board b WHERE b.category = :category AND b.delYn = :delYn AND " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchQuery, '%'))")
    Page<Board> findByCategoryAndDelYnAndTitleContainingIgnoreCase(
            Category category, DelYN delYn, String searchQuery, Pageable pageable);

    @Query("SELECT b FROM Board b WHERE b.category = :category AND b.delYn = :delYn AND " +
            "LOWER(b.content) LIKE LOWER(CONCAT('%', :searchQuery, '%'))")
    Page<Board> findByCategoryAndDelYnAndContentContainingIgnoreCase(
            Category category, DelYN delYn, String searchQuery, Pageable pageable);

    // 태그 이름으로 검색하고 카테고리와 삭제 여부에 따라 필터링
    @Query("SELECT b FROM Board b JOIN b.boardTags bt JOIN bt.boardTags t WHERE b.category = :category AND b.delYn = :delYn AND " +
            "LOWER(t.tag) LIKE LOWER(CONCAT('%', :searchQuery, '%'))")
    Page<Board> findByCategoryAndDelYnAndTagsContainingIgnoreCase(
            Category category, DelYN delYn, String searchQuery, Pageable pageable);

    @Query("SELECT b FROM Board b WHERE b.category = :category AND b.delYn = :delYn AND " +
            "(LOWER(b.title) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR LOWER(b.content) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
    Page<Board> findByCategoryAndDelYnAndTitleOrContentContainingIgnoreCase(
            Category category, DelYN delYn, String searchQuery, Pageable pageable);

    // 소프트 삭제
    @Modifying
    @Transactional
    @Query("UPDATE Board b SET b.delYn = 'Y' WHERE b.id = :boardId")
    void softDeleteById(Long boardId);

}
