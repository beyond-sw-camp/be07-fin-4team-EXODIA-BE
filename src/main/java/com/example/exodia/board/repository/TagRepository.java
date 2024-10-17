package com.example.exodia.board.repository;

import com.example.exodia.board.domain.BoardTags;
import org.springframework.data.jpa.repository.JpaRepository;

// Tags 엔티티를 관리하는 JPA Repository
public interface TagRepository extends JpaRepository<BoardTags, Long> {

}
