package com.example.exodia.comment.repository;

import com.example.exodia.comment.domain.Comment;
import com.example.exodia.common.domain.DelYN;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 게시물 ID로 댓글 조회
    List<Comment> findByBoardId(Long boardId);

    // 게시물 ID와 DelYN으로 댓글 조회
    List<Comment> findByBoardIdAndDelYn(Long boardId, DelYN delYn);

    List<Comment> findByQnaId(Long QnaId);

    @Modifying
    @Query("update Comment c set c.delYn = 'Y', c.deletedAt = CURRENT_TIMESTAMP where c.id = :id")
    void markAsDeleted(@Param("id") Long id);
}
