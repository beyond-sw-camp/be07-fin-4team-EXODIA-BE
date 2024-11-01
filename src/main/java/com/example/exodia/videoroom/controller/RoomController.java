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
}
