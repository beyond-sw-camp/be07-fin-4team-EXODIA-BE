package com.example.exodia.videoroom.service;

import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.videoroom.domain.Participant;
import com.example.exodia.videoroom.domain.Room;
import com.example.exodia.videoroom.repository.ParticipantRepository;
import com.example.exodia.videoroom.repository.RoomRepository;
import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.OpenViduJavaClientException;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.Session;
import io.openvidu.java.client.Connection;
import io.openvidu.java.client.ConnectionProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class RoomService {

    private final OpenViduService openViduService;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;

    @Autowired
    public RoomService(OpenViduService openViduService, RoomRepository roomRepository, UserRepository userRepository, ParticipantRepository participantRepository) {
        this.openViduService = openViduService;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
    }

    public Room createRoom(String title) throws OpenViduJavaClientException, OpenViduHttpException {
        String sessionId = openViduService.createSession();
        Room room = new Room();
        room.setTitle(title);
        room.setSessionId(sessionId);
        return roomRepository.save(room);
    }

    public String joinRoom(String sessionId, Long userId) throws OpenViduJavaClientException, OpenViduHttpException {
        Room room = roomRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Participant participant = new Participant();
        participant.setUser(user);
        participant.setRoom(room);
        participantRepository.save(participant);

        room.addParticipant(participant);
        roomRepository.save(room);

        return openViduService.createConnection(sessionId);
    }

    public void deleteRoom(String sessionId) throws OpenViduJavaClientException, OpenViduHttpException {
        Room room = roomRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        roomRepository.delete(room);
        openViduService.closeSession(sessionId);
    }

    public List<Room> getRoomList() {
        return roomRepository.findAll();
    }
}