package com.example.exodia.videoroom.repository;

import com.example.exodia.user.domain.User;
import com.example.exodia.videoroom.domain.Participant;
import com.example.exodia.videoroom.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByRoom(Room room);
    Optional<Participant> findByRoomAndUser(Room room, User user);
    Optional<Participant> findByUserAndRoom(User user, Room room);
    Optional<User> findByUserNum(String userNum);
}