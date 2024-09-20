package com.example.exodia.video.repository;

import com.example.exodia.video.domain.VideoRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<VideoRoom, Long> {
    Optional<VideoRoom> findByRoomName(String roomName);
}
