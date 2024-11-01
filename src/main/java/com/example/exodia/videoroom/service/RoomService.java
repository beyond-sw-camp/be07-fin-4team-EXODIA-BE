package com.example.exodia.videoroom.service;

import com.example.exodia.videoroom.domain.Participant;
import com.example.exodia.videoroom.domain.Room;
import com.example.exodia.user.domain.User;
import com.example.exodia.videoroom.repository.ParticipantRepository;
import com.example.exodia.videoroom.repository.RoomRepository;
import com.example.exodia.user.repository.UserRepository;
import io.openvidu.java.client.OpenViduJavaClientException;
import io.openvidu.java.client.OpenViduHttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private OpenViduService openViduService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private UserRepository userRepository;

    // 방 생성
    public Room createRoom(String title) throws OpenViduJavaClientException, OpenViduHttpException {
        String sessionId = openViduService.createSession();
        Room room = new Room();
        room.setTitle(title);
        room.setSessionId(sessionId);
        return roomRepository.save(room);
    }

    // 참가자 추가
    public String joinRoom(String sessionId, Long userId) throws OpenViduJavaClientException, OpenViduHttpException {
        Optional<Room> optionalRoom = roomRepository.findBySessionId(sessionId);
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalRoom.isPresent() && optionalUser.isPresent()) {
            Room room = optionalRoom.get();
            User user = optionalUser.get();
            String token = openViduService.createConnection(sessionId);

            Participant participant = new Participant();
            participant.setUser(user);
            participant.setRoom(room);
            participantRepository.save(participant);

            return token;
        } else {
            throw new RuntimeException("Room or User not found");
        }
    }

    // 방 삭제
    public void deleteRoom(String sessionId) throws OpenViduJavaClientException, OpenViduHttpException {
        Optional<Room> optionalRoom = roomRepository.findBySessionId(sessionId);
        if (optionalRoom.isPresent()) {
            openViduService.closeSession(sessionId);
            roomRepository.delete(optionalRoom.get());
        }
    }
}
