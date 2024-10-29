package com.example.exodia.videoroom.service;

import com.example.exodia.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import com.example.exodia.user.domain.User;
import com.example.exodia.videoroom.domain.Room;
import com.example.exodia.videoroom.domain.Participant;
import com.example.exodia.videoroom.repository.RoomRepository;
import com.example.exodia.videoroom.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private UserRepository userRepository;

    private final String OPENVIDU_URL = "http://localhost:4443";
    private final String OPENVIDU_SECRET = "MY_SECRET";

    public Room createRoom(String roomName, String password) {
        Room room = Room.builder()
                .roomName(roomName)
                .password(password)
                .participantCount(1)
                .sessionId(generateSessionId()) // OpenVidu 세션 생성 후 세션 ID 할당
                .build();
        return roomRepository.save(room);
    }

    private String generateSessionId() {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("OPENVIDUAPP", OPENVIDU_SECRET);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<>("{}", headers);
        String sessionUrl = OPENVIDU_URL + "/api/sessions";

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    sessionUrl, HttpMethod.POST, requestEntity, Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return (String) response.getBody().get("id");
            } else {
                throw new RuntimeException("OpenVidu 세션 생성에 실패했습니다. 상태 코드: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("OpenVidu 세션 생성 요청 중 오류 발생: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void addParticipant(Room room, User user) {
        if (room == null || user == null) {
            throw new IllegalArgumentException("Room and User must be non-null.");
        }
        Participant participant = Participant.builder()
                .room(room)
                .user(user)
                .build();
        participantRepository.save(participant);
    }

    public void addParticipantToRoom(Long roomId, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Participant participant = new Participant();
        participant.setRoom(room);
        participant.setUser(user);

        participantRepository.save(participant);
        room.incrementParticipant();
        roomRepository.save(room);
    }

    @Transactional
    public void removeParticipant(Room room, User user) {
        Participant participant = participantRepository.findByRoomAndUser(room, user)
                .orElseThrow(() -> new IllegalArgumentException("참가자가 존재하지 않습니다."));

        participantRepository.delete(participant);
        room.decrementParticipant();
        roomRepository.save(room);

        if (room.getParticipantCount() == 0) {
            roomRepository.delete(room);
        }
    }



    public List<Room> getRoomList() {
        return roomRepository.findAll();
    }

    public void deleteRoom(Long roomId) {
        roomRepository.deleteById(roomId);
    }

    public List<Participant> getParticipants(Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        return participantRepository.findByRoom(room);
    }

    public boolean validatePassword(Long roomId, String password) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("방을 찾을 수 없습니다."));
        return room.getPassword() != null && room.getPassword().equals(password);
    }
}
