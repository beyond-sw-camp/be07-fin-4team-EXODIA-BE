package com.example.exodia.videoroom.controller;

import com.example.exodia.videoroom.domain.Room;
import com.example.exodia.videoroom.service.RoomService;
import io.openvidu.java.client.OpenViduJavaClientException;
import io.openvidu.java.client.OpenViduHttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    // 방 생성
    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createRoom(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String userNum = request.get("userNum");

        if (title == null || userNum == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            Map<String, String> roomInfo = roomService.createRoom(title, userNum);
            return new ResponseEntity<>(roomInfo, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 참가자 추가
    @PostMapping("/{sessionId}/leave")
    public ResponseEntity<Void> leaveRoom(@PathVariable String sessionId, @RequestParam String userNum) {
        try {
            roomService.leaveRoom(sessionId, userNum);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 방 삭제
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String sessionId) {
        try {
            roomService.deleteRoom(sessionId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<Room>> getRoomList() {
        List<Room> rooms = roomService.getRoomList();
        return ResponseEntity.ok(rooms);
    }

    // 참가자 추가 (join)
    @PostMapping("/{sessionId}/join")
    public ResponseEntity<Map<String, String>> joinRoom(@PathVariable String sessionId, @RequestParam String userNum) {
        try {
            String token = roomService.joinRoom(sessionId, userNum);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

