package com.example.exodia.board.repository;

import com.example.exodia.board.domain.Board;
import com.example.exodia.board.domain.Category;
import com.example.exodia.common.domain.DelYN;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 고정되지 않은 게시글만 필터링하여 카테고리와 삭제 여부로 검색
    Page<Board> findByCategoryAndDelYnAndIsPinnedFalse(Category category, DelYN delYN, Pageable pageable);

    Optional<Board> findById(Long boardId);

    // 고정되지 않은 게시글에서 제목을 검색
    @Query("SELECT b FROM Board b WHERE b.category = :category AND b.delYn = :delYn AND b.isPinned = false AND " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchQuery, '%'))")
    Page<Board> findByCategoryAndDelYnAndIsPinnedFalseAndTitleContainingIgnoreCase(
            Category category, DelYN delYn, String searchQuery, Pageable pageable);

    // 고정되지 않은 게시글에서 내용을 검색
    @Query("SELECT b FROM Board b WHERE b.category = :category AND b.delYn = :delYn AND b.isPinned = false AND " +
            "LOWER(b.content) LIKE LOWER(CONCAT('%', :searchQuery, '%'))")
    Page<Board> findByCategoryAndDelYnAndIsPinnedFalseAndContentContainingIgnoreCase(
            Category category, DelYN delYn, String searchQuery, Pageable pageable);

    // 고정되지 않은 게시글에서 태그를 검색
    @Query("SELECT b FROM Board b JOIN b.boardTags bt JOIN bt.boardTags t WHERE b.category = :category AND b.delYn = :delYn AND b.isPinned = false AND " +
            "LOWER(t.tag) LIKE LOWER(CONCAT('%', :searchQuery, '%'))")
    Page<Board> findByCategoryAndDelYnAndIsPinnedFalseAndTagsContainingIgnoreCase(
            Category category, DelYN delYn, String searchQuery, Pageable pageable);

    // 고정되지 않은 게시글에서 제목 또는 내용을 검색
    @Query("SELECT b FROM Board b WHERE b.category = :category AND b.delYn = :delYn AND b.isPinned = false AND " +
            "(LOWER(b.title) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR LOWER(b.content) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
    Page<Board> findByCategoryAndDelYnAndIsPinnedFalseAndTitleOrContentContainingIgnoreCase(
            Category category, DelYN delYn, String searchQuery, Pageable pageable);

    // 고정된 게시글만 가져오는 메서드
    List<Board> findByIsPinnedTrue(Sort sort);

    // 소프트 삭제 메서드
    @Modifying
    @Transactional
    @Query("UPDATE Board b SET b.delYn = 'Y' WHERE b.id = :boardId")
    void softDeleteById(Long boardId);

}
