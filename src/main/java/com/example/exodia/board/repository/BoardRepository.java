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

    // 타이틀 또는 내용으로 검색하고 카테고리와 삭제 여부에 따라 필터링
    Page<Board> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndCategoryAndDelYn(
            String title, String content, Category category, DelYN delYN, Pageable pageable);

    // 사번으로 검색하고 카테고리와 삭제 여부에 따라 필터링
    Page<Board> findByUser_UserNumAndCategoryAndDelYn(String userNum, Category category, DelYN delYN, Pageable pageable);

    // 이름으로 검색하고 카테고리와 삭제 여부에 따라 필터링
    Page<Board> findByUser_NameContainingIgnoreCaseAndCategoryAndDelYn(String name, Category category, DelYN delYN, Pageable pageable);

    // 카테고리와 삭제 여부에 따라 필터링
    Page<Board> findByCategoryAndDelYn(Category category, DelYN delYN, Pageable pageable);

    // 카테고리로 검색하되 고정된 게시글을 최우선으로 정렬하여 반환
    @Query("SELECT b FROM Board b WHERE b.delYn = 'N' AND b.category = :category ORDER BY b.isPinned DESC, b.createdAt DESC")
    Page<Board> findAllWithPinnedByCategory(Category category, Pageable pageable);

    Optional<Board> findById(Long boardId);
}
