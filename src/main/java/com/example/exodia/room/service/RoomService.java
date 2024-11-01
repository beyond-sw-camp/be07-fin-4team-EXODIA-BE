package com.example.exodia.room.service;

import com.example.exodia.room.domain.Room;
import com.example.exodia.room.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
        this.restTemplate = new RestTemplate();
    }

    public Room createRoom(String roomName, String password) {
        Room room = Room.builder()
                .roomName(roomName)
                .password(password)
                .participantCount(0)
                .build();

        // Janus에 방 생성 요청 보내기
        // 여기에 Janus WebSocket 또는 HTTP API로 연결하여 방 생성 구현
        String janusApiUrl = "https://your-janus-url:8089/janus";
        String response = restTemplate.postForObject(janusApiUrl + "/create", null, String.class);

        // 방 생성 성공 시 데이터베이스에 저장
        return roomRepository.save(room);
    }

    public void joinRoom(Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        room.incrementParticipantCount();
        roomRepository.save(room);
    }

    public void leaveRoom(Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        room.decrementParticipantCount();
        roomRepository.save(room);

        // 참가자 수가 0명이면 Janus API를 통해 방 삭제
        if (room.getParticipantCount() <= 0) {
            roomRepository.delete(room);
            deleteRoomFromJanus(roomId);
        }
    }

    private void deleteRoomFromJanus(Long roomId) {
        // Janus API로 방 삭제 요청
        String janusApiUrl = "https://your-janus-url:8089/janus";
        restTemplate.delete(janusApiUrl + "/{roomId}", roomId);
    }
}
