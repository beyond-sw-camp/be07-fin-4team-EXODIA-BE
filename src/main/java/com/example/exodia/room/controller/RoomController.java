package com.example.exodia.room.controller;

import com.example.exodia.room.domain.Room;
import com.example.exodia.room.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/create")
    public Room createRoom(@RequestParam String roomName, @RequestParam(required = false) String password) {
        return roomService.createRoom(roomName, password);
    }

    @PostMapping("/{roomId}/join")
    public void joinRoom(@PathVariable Long roomId) {
        roomService.joinRoom(roomId);
    }

    @PostMapping("/{roomId}/leave")
    public void leaveRoom(@PathVariable Long roomId) {
        roomService.leaveRoom(roomId);
    }
}
