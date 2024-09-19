package com.example.exodia.position.repository;

import com.example.exodia.position.domain.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {
}
