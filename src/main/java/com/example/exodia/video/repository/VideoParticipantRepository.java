package com.example.exodia.video.repository;

import com.example.exodia.video.domain.VideoParticipant;
import com.example.exodia.video.domain.VideoRoom;
import com.example.exodia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoParticipantRepository extends JpaRepository<VideoParticipant, Long> {
    Optional<VideoParticipant> findByRoomAndUser(VideoRoom room, User user);

    long countByRoom(VideoRoom room);
}
