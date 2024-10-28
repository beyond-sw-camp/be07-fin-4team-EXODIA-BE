package com.example.exodia.videoroom.repository;

import com.example.exodia.user.domain.User;
import com.example.exodia.videoroom.domain.Participant;
import com.example.exodia.videoroom.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Participant findByRoomAndUser(Room room, User user);
    List<Participant> findByRoom(Room room); //
}