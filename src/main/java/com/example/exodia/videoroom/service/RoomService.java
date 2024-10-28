package com.example.exodia.videoroom.service;


import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.videoroom.domain.Participant;
import com.example.exodia.videoroom.domain.Room;
import com.example.exodia.videoroom.repository.ParticipantRepository;
import com.example.exodia.videoroom.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ParticipantRepository participantRepository;


    public Room createRoom(String roomName, String password) {
        Room room = Room.builder()
                .roomName(roomName)
                .password(password)
                .participantCount(1)
                .build();
        return roomRepository.save(room);
    }

    public List<Room> getRoomList() {
        return roomRepository.findAll();
    }

    public void deleteRoom(Long roomId) {
        roomRepository.deleteById(roomId);
    }

    public void updateParticipantCount(Long roomId, int count) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setParticipantCount(room.getParticipantCount() + count);
        if (room.getParticipantCount() <= 0) {
            roomRepository.delete(room);
        } else {
            roomRepository.save(room);
        }
    }

    public Participant enterRoom(Long roomId, String userNum) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User user = userRepository.findByUserNum(userNum).orElseThrow();

        Participant participant = Participant.builder()
                .room(room)
                .user(user)
                .build();
        participantRepository.save(participant);

        room.incrementParticipant();
        roomRepository.save(room);
        return participant;
    }

    // 방 퇴장
    public void leaveRoom(Long roomId, String userNum) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User user = userRepository.findByUserNum(userNum).orElseThrow();

        Participant participant = participantRepository.findByRoomAndUser(room, user);
        if (participant != null) {
            participantRepository.delete(participant);
            room.decrementParticipant();
            if (room.getParticipantCount() <= 0) {
                roomRepository.delete(room);
            } else {
                roomRepository.save(room);
            }
        }
    }

    public List<Participant> getParticipants(Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        return participantRepository.findByRoom(room);
    }
}
