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

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    @Transactional
    public Room createRoom(String title) throws OpenViduJavaClientException, OpenViduHttpException {
        String sessionId = openViduService.createSession();
        Room room = new Room();
        room.setTitle(title);
        room.setSessionId(sessionId);
        return roomRepository.save(room);
    }

    public String joinRoom(String sessionId, String userNum) throws OpenViduJavaClientException, OpenViduHttpException {
        Room room = roomRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("User not found with userNum: " + userNum));

        Participant participant = participantRepository.findByUser_UserNumAndRoom(userNum, room)
                .orElseGet(() -> {
                    Participant newParticipant = new Participant();
                    newParticipant.setUser(user);
                    newParticipant.setRoom(room);
                    return participantRepository.save(newParticipant);
                });

        room.addParticipant(participant);
        roomRepository.save(room);

        String token = openViduService.createConnection(sessionId);
        participant.setToken(token);
        participantRepository.save(participant);

        return token;
    }




    @Transactional
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