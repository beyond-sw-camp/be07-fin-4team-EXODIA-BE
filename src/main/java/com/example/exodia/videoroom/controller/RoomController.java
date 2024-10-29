package com.example.exodia.videoroom.controller;

import com.example.exodia.videoroom.domain.Participant;
import com.example.exodia.videoroom.domain.Room;
import com.example.exodia.videoroom.dto.RoomRequestDto;
import com.example.exodia.videoroom.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createRoom(@RequestBody RoomRequestDto roomRequestDto) {
        Room room = roomService.createRoom(roomRequestDto.getRoomName(), roomRequestDto.getPassword());

        Map<String, Object> response = new HashMap<>();
        response.put("id", room.getId());
        response.put("sessionId", room.getSessionId());
        response.put("roomName", room.getRoomName());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/list")
    public ResponseEntity<List<Room>> getRoomList() {
        List<Room> rooms = roomService.getRoomList();
        return ResponseEntity.ok(rooms);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roomId}/participants")
    public ResponseEntity<List<Participant>> getParticipants(@PathVariable Long roomId) {
        List<Participant> participants = roomService.getParticipants(roomId);
        return ResponseEntity.ok(participants);
    }


    @PostMapping("/{roomId}/join")
    public ResponseEntity<String> joinRoom(@PathVariable Long roomId, @RequestBody Map<String, String> payload) {
        String password = payload.get("password");
        Long userId = Long.parseLong(payload.get("userId")); // userId를 payload에서 가져옴

        boolean isAuthorized = roomService.validatePassword(roomId, password);

        if (isAuthorized) {
            roomService.addParticipantToRoom(roomId, userId);
            return ResponseEntity.ok("방에 참가 완료");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("비밀번호가 일치하지 않습니다.");
        }
    }

}
