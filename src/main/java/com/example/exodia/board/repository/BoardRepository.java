package com.example.exodia.board.repository;

import com.example.exodia.board.domain.Board;
import com.example.exodia.board.domain.Category;
import com.example.exodia.common.domain.DelYN;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 타이틀로 검색하고 카테고리와 삭제 여부에 따라 필터링
    Page<Board> findByTitleContainingIgnoreCaseAndCategoryAndDelYn(String title, Category category, DelYN delYN, Pageable pageable);

    // 내용으로 검색하고 카테고리와 삭제 여부에 따라 필터링
    Page<Board> findByContentContainingIgnoreCaseAndCategoryAndDelYn(String content, Category category, DelYN delYN, Pageable pageable);

    // 태그로 검색하고 카테고리와 삭제 여부에 따라 필터링 (연관된 태그 테이블과 조인하여 태그 검색)
    @Query("SELECT b FROM Board b JOIN b.tags t WHERE LOWER(t.tag) LIKE LOWER(CONCAT('%', :tag, '%')) AND b.category = :category AND b.delYn = :delYN")
    Page<Board> findByTagAndCategoryAndDelYn(String tag, Category category, DelYN delYN, Pageable pageable);

    // 태그로 검색하고 카테고리와 삭제 여부에 따라 필터링 (기존 방식의 태그 이름 포함 검색)
    @Query("SELECT b FROM Board b WHERE LOWER(b.tags) LIKE LOWER(CONCAT('%', :tags, '%')) AND b.category = :category AND b.delYn = :delYN")
    Page<Board> findByTagsContainingIgnoreCaseAndCategoryAndDelYn(String tags, Category category, DelYN delYN, Pageable pageable);

    // 카테고리와 삭제 여부에 따라 필터링
    Page<Board> findByCategoryAndDelYn(Category category, DelYN delYN, Pageable pageable);

    // 카테고리로 검색하되 고정된 게시글을 최우선으로 정렬하여 반환
    @Query("SELECT b FROM Board b WHERE b.delYn = 'N' AND b.category = :category ORDER BY b.isPinned DESC, b.createdAt DESC")
    Page<Board> findAllWithPinnedByCategory(Category category, Pageable pageable);

    Optional<Board> findById(Long boardId);
}
