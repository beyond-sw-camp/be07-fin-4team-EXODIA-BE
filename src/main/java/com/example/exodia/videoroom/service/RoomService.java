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

import java.util.*;

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
    public Map<String, String> createRoom(String title, String userNum,String password) throws OpenViduJavaClientException, OpenViduHttpException {
        // 세션 생성
        String sessionId = openViduService.createSession();

        Room room = new Room();
        room.setTitle(title);
        room.setSessionId(sessionId);
        room.setPassword(password);
        room = roomRepository.save(room);

        String userName = getUserNameByUserNum(userNum);
        String token = joinRoom(sessionId, userNum, userName);

        Map<String, String> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("token", token);
        return response;
    }

    public Room findBySessionId(String sessionId) {
        return roomRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Room not found for sessionId: " + sessionId));
    }


    public String joinRoom(String sessionId, String userNum, String userName) throws OpenViduJavaClientException, OpenViduHttpException {
        Room room = roomRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<Participant> existingParticipant = participantRepository.findByUserAndRoom(user, room);
        if (existingParticipant.isPresent()) {
            return existingParticipant.get().getToken();
        }

        String token = openViduService.createConnection(sessionId, userName);

        Participant participant = new Participant();
        participant.setUser(user);
        participant.setRoom(room);
        participant.setToken(token);

        participantRepository.save(participant);
        room.addParticipant(participant);
        roomRepository.save(room);

        return token;
    }


    @Transactional
    public void leaveRoom(String sessionId, String userNum) {
        Room room = roomRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Participant participant = participantRepository.findByUserAndRoom(user, room)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        room.removeParticipant(participant);
        participantRepository.delete(participant);

        if (room.getParticipantCount() == 0) {
            roomRepository.delete(room);
        } else {
            roomRepository.save(room);
        }
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


    public String getUserNameByUserNum(String userNum) {
        return userRepository.findByUserNum(userNum)
                .map(User::getName)
                .orElse("Unknown User");
    }

}