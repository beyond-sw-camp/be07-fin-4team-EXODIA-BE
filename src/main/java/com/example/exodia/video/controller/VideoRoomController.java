package com.example.exodia.video.controller;

import com.example.exodia.common.config.RedisMessagePublisher;
import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.user.domain.CustomUserDetails;
import com.example.exodia.user.domain.User;
import com.example.exodia.video.domain.VideoRoom;
import com.example.exodia.video.dto.JoinRoomDto;
import com.example.exodia.video.service.VideoRoomService;
import com.example.exodia.video.dto.CreateRoomDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video")
public class VideoRoomController {

    private final VideoRoomService videoRoomService;
    private final RedisMessagePublisher messagePublisher;

    public VideoRoomController(VideoRoomService videoRoomService, RedisMessagePublisher messagePublisher) {
        this.videoRoomService = videoRoomService;
        this.messagePublisher = messagePublisher;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomDto roomDto, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User host = userDetails.getUser();
        Long janusRoomId = roomDto.getJanusRoomId();  
        VideoRoom room = videoRoomService.createRoom(roomDto.getRoomName(), roomDto.getPassword(), janusRoomId, host);
        videoRoomService.joinRoom(room.getJanusRoomId(), roomDto.getPassword(), host);

        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "방 생성 및 입장 완료", room));
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinRoom(@RequestBody JoinRoomDto joinRoomDto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."), HttpStatus.UNAUTHORIZED);
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (userDetails == null) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, "사용자 정보가 없습니다."), HttpStatus.BAD_REQUEST);
        }

        User user = userDetails.getUser();
        if (user == null) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, "사용자 정보가 없습니다."), HttpStatus.BAD_REQUEST);
        }

        if (joinRoomDto == null || joinRoomDto.getJanusRoomId() == null) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, "방 ID가 없습니다."), HttpStatus.BAD_REQUEST);
        }

        Long janusRoomId = joinRoomDto.getJanusRoomId();
        String password = joinRoomDto.getPassword();

        boolean joined = videoRoomService.joinRoom(janusRoomId, password, user);
        if (!joined) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.NOT_FOUND, "방이 존재하지 않거나 비밀번호가 틀렸습니다."), HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "방 입장 성공", null));
    }

    @GetMapping("/list")
    public ResponseEntity<?> listRooms() {
        List<VideoRoom> rooms = videoRoomService.getActiveRooms();
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "방 목록 조회 성공", rooms));
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leaveRoom(@RequestParam Long janusRoomId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        videoRoomService.leaveRoom(janusRoomId, user);
        messagePublisher.publish(user.getName() + " left room: " + janusRoomId);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "방 퇴장 성공", null));
    }
}
