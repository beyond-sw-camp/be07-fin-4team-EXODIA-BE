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

    // 타이틀로 검색하고 카테고리와 삭제 여부에 따라 필터링
    Page<Board> findByTitleContainingIgnoreCaseAndCategoryAndDelYn(String title, Category category, DelYN delYN, Pageable pageable);

    // 내용으로 검색하고 카테고리와 삭제 여부에 따라 필터링
    Page<Board> findByContentContainingIgnoreCaseAndCategoryAndDelYn(String content, Category category, DelYN delYN, Pageable pageable);

    // 태그 ID로 검색하고 카테고리와 삭제 여부에 따라 필터링 (BoardTag 테이블과 조인하여 태그 검색)
    @Query("SELECT b FROM Board b JOIN b.boardTags bt JOIN bt.boardTags t WHERE t.id IN :tagIds AND b.category = :category AND b.delYn = :delYN")
    Page<Board> findByTagIdsAndCategoryAndDelYn(List<Long> tagIds, Category category, DelYN delYN, Pageable pageable);

    // 태그 이름으로 검색하고 카테고리와 삭제 여부에 따라 필터링
    @Query("SELECT b FROM Board b JOIN b.boardTags bt JOIN bt.boardTags t WHERE LOWER(t.tag) LIKE LOWER(CONCAT('%', :tagName, '%')) AND b.category = :category AND b.delYn = :delYN")
    Page<Board> findByTagsContainingIgnoreCaseAndCategoryAndDelYn(String tagName, Category category, DelYN delYN, Pageable pageable);

    // 카테고리와 삭제 여부에 따라 필터링
    Page<Board> findByCategoryAndDelYn(Category category, DelYN delYN, Pageable pageable);

    // 카테고리로 검색하되 고정된 게시글을 최우선으로 정렬하여 반환
    @Query("SELECT b FROM Board b WHERE b.delYn = 'N' AND b.category = :category ORDER BY b.isPinned DESC, b.createdAt DESC")
    Page<Board> findAllWithPinnedByCategory(Category category, Pageable pageable);

    Optional<Board> findById(Long boardId);

    // 타이틀 또는 내용으로 검색하고 카테고리와 삭제 여부에 따라 필터링
    Page<Board> findByTitleContainingOrContentContainingIgnoreCaseAndCategoryAndDelYn(
            String title, String content, Category category, DelYN delYN, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Board b SET b.delYn = 'Y' WHERE b.id = :boardId")
    void softDeleteById(Long boardId);

}
