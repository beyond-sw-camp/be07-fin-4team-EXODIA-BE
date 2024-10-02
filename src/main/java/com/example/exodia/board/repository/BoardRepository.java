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

    Page<Board> findByTitleContainingIgnoreCaseAndCategory(String title, Category category, DelYN delYN, Pageable pageable);

    Page<Board> findByContentContainingIgnoreCaseAndCategory(String content, Category category, DelYN delYN, Pageable pageable);

    Page<Board> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndCategory(
            String title, String content, Category category, DelYN delYN, Pageable pageable);

    Page<Board> findByUser_UserNumAndCategoryAndDelYn(String userNum, Category category, DelYN delYN, Pageable pageable);

    Page<Board> findByUser_NameContainingIgnoreCaseAndCategory(String name, Category category, DelYN delYN, Pageable pageable);

    Page<Board> findByCategoryAndDelYn(Category category, DelYN delYN, Pageable pageable);

    @Query("SELECT b FROM Board b WHERE b.delYn = 'N' AND b.category = :category ORDER BY b.isPinned DESC, b.createdAt DESC")
    Page<Board> findAllWithPinnedByCategory(Category category, Pageable pageable);

    Optional<Board> findById(Long boardId);
}
