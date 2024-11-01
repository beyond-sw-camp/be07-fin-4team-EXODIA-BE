package com.example.exodia.videoroom.service;

import com.example.exodia.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
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
    private OpenViduService openViduService;

    public Room createRoom(String roomName, String password) {
        String sessionId = openViduService.createSession();
        Room room = new Room(roomName, password, sessionId);
        return roomRepository.save(room);
    }

    public List<Room> getRoomList() {
        return roomRepository.findAll();
    }
}
