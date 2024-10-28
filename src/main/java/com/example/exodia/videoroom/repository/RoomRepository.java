package com.example.exodia.videoroom.repository;

import com.example.exodia.videoroom.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Override
    Optional<Room> findById(Long aLong);
}