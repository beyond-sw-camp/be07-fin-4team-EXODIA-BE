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

        System.out.println("Room Name: " + roomDto.getRoomName());
        VideoRoom room = videoRoomService.createRoom(roomDto.getRoomName(), roomDto.getPassword(), host);
        videoRoomService.joinRoom(room.getRoomName(), roomDto.getPassword(), host);
        messagePublisher.publish("Room created and joined by: " + host.getName() + " for room: " + roomDto.getRoomName());
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "방 생성 및 입장 완료", room));
    }


    @PostMapping("/join")
    public ResponseEntity<?> joinRoom(@RequestBody JoinRoomDto joinRoomDto, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            if (userDetails == null) {
                System.out.println("UserDetails가 null입니다.");
                return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, "유저 정보가 없습니다."), HttpStatus.BAD_REQUEST);
            }

            User user = userDetails.getUser();

            if (user == null) {
                System.out.println("로그인한 유저 정보가 없습니다.");
                return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, "로그인 유저가 없습니다."), HttpStatus.BAD_REQUEST);
            }

            String roomName = joinRoomDto.getRoomName();
            String password = joinRoomDto.getPassword();

            if (roomName == null || roomName.isEmpty()) {
                System.out.println("방 이름이 없습니다.");
                return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, "방 이름이 필요합니다."), HttpStatus.BAD_REQUEST);
            }

            System.out.println("join request for room: " + roomName + " with password: " + password);

            boolean joined = videoRoomService.joinRoom(roomName, password, user);
            if (joined) {
                messagePublisher.publish(user.getName() + " joined room: " + roomName);
                return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "방 입장 성공", null));
            } else {
                return new ResponseEntity<>(new CommonErrorDto(HttpStatus.FORBIDDEN, "비밀번호가 틀렸거나 방이 존재하지 않습니다."), HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @GetMapping("/list")
    public ResponseEntity<?> listRooms() {
        List<VideoRoom> rooms = videoRoomService.getActiveRooms();
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "방 목록 조회 성공", rooms));
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leaveRoom(@RequestParam String roomName, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        videoRoomService.leaveRoom(roomName, user);
        messagePublisher.publish(user.getName() + " left room: " + roomName);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "방 퇴장 성공", null));
    }
}

