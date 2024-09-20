package com.example.exodia.video.controller;

import com.example.exodia.common.auth.JwtTokenProvider;
import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.video.dto.CreateRoomDto;
import com.example.exodia.video.dto.JoinRoomDto;
import com.example.exodia.video.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // 방 생성
    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@RequestHeader("Authorization") String token, @RequestBody CreateRoomDto createRoomDto) {
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                CommonErrorDto errorDto = new CommonErrorDto(HttpStatus.UNAUTHORIZED, "Invalid token");
                return new ResponseEntity<>(errorDto, HttpStatus.UNAUTHORIZED);
            }

            String userNum = jwtTokenProvider.getUserNumFromToken(token);
            videoService.createRoom(createRoomDto, userNum);
            CommonResDto successDto = new CommonResDto(HttpStatus.OK, "온라인 회의룸이 생성되었습니다.", null);
            return new ResponseEntity<>(successDto, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
            CommonErrorDto errorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            CommonErrorDto errorDto = new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "온라인 회의 생성을 실패했습니다");
            return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 방 참가
    @PostMapping("/join")
    public ResponseEntity<?> joinRoom(@RequestHeader("Authorization") String token, @RequestBody JoinRoomDto joinRoomDto) {
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                CommonErrorDto errorDto = new CommonErrorDto(HttpStatus.UNAUTHORIZED, "Invalid token");
                return new ResponseEntity<>(errorDto, HttpStatus.UNAUTHORIZED);
            }

            String userNum = jwtTokenProvider.getUserNumFromToken(token);
            videoService.joinRoom(joinRoomDto, userNum);
            CommonResDto successDto = new CommonResDto(HttpStatus.OK, "온라인 회의룸에 참여합니다.", null);
            return new ResponseEntity<>(successDto, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
            CommonErrorDto errorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            CommonErrorDto errorDto = new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "온라인 회의 참가 실패");
            return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 방 퇴장
    @PostMapping("/leave")
    public ResponseEntity<?> leaveRoom(@RequestHeader("Authorization") String token, @RequestParam String roomName) {
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                CommonErrorDto errorDto = new CommonErrorDto(HttpStatus.UNAUTHORIZED, "Invalid token");
                return new ResponseEntity<>(errorDto, HttpStatus.UNAUTHORIZED);
            }

            String userNum = jwtTokenProvider.getUserNumFromToken(token);
            videoService.leaveRoom(roomName, userNum);
            CommonResDto successDto = new CommonResDto(HttpStatus.OK, "온라인 회의룸 나갑니다.", null);
            return new ResponseEntity<>(successDto, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
            CommonErrorDto errorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            CommonErrorDto errorDto = new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "온라인 회의 탈출 실패");
            return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
