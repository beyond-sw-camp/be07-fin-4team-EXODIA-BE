package com.example.exodia.videoroom.service;

import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.videoroom.domain.Participant;
import com.example.exodia.videoroom.domain.Room;
import com.example.exodia.videoroom.repository.ParticipantRepository;
import com.example.exodia.videoroom.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Service
public class RoomService {

    private final String OPENVIDU_URL = "http://localhost:4443";
    private final String OPENVIDU_SECRET = "MY_SECRET";

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
                .participantCount(0)
                .sessionId(generateSessionId())
                .build();
        return roomRepository.save(room);
    }

    private String generateSessionId() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("OPENVIDUAPP", OPENVIDU_SECRET);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> request = new HttpEntity<>("{}", headers);
        String sessionUrl = OPENVIDU_URL + "/api/sessions";

        ResponseEntity<Map> response = restTemplate.exchange(
                sessionUrl, HttpMethod.POST, request, Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return (String) response.getBody().get("id"); // 생성된 sessionId 반환
        } else {
            throw new RuntimeException("세션 생성 실패");
        }
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
