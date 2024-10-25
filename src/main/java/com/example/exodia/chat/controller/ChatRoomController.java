package com.example.exodia.chat.controller;

import com.example.exodia.chat.dto.ChatRoomExistResponse;
import com.example.exodia.chat.dto.ChatRoomRequest;
import com.example.exodia.chat.dto.ChatUserRequest;
import com.example.exodia.chat.service.ChatRoomService;
import com.example.exodia.common.dto.CommonResDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/chatRoom")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    @Autowired
    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    // 채팅방 생성
    @PostMapping("/create")
    public ResponseEntity<?> chatRoomCreate(@RequestBody ChatRoomRequest dto){
        ChatRoomExistResponse chatRoomExistResponse = chatRoomService.createChatRoom(dto);
        CommonResDto commonResDto = new CommonResDto();
        if(chatRoomExistResponse.isExistCheck()){
            commonResDto = new CommonResDto(HttpStatus.OK, "채팅방이 이미 존재합니다.", chatRoomExistResponse);
        }else{
            commonResDto = new CommonResDto(HttpStatus.OK, "채팅방이 생성되었습니다.", chatRoomExistResponse);
        }
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 채팅방 목록 조회
    @GetMapping("/list/{userNum}")
    public ResponseEntity<?> chatRoomList(@PathVariable("userNum") String userNum){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "채팅방목록이 조회되었습니다.", chatRoomService.viewChatRoomList(userNum));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 채팅방 검색
    @GetMapping("/search")
    public ResponseEntity<?> chatRoomSearchList(@RequestParam(required = false) String userNum,
                                                @RequestParam(required = false) String searchValue){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "채팅방목록이 조회되었습니다.", chatRoomService.searchChatRoom(userNum, searchValue));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @GetMapping("/alarm")
    public ResponseEntity<?> getChatAlarm(){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "채팅 알람개수가 조회되었습니다.", chatRoomService.getChatAlarm());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 채팅방 메세지 조회 == 채팅방 입장
    @GetMapping("/{roomId}")
    public ResponseEntity<?> chatRoomView(@PathVariable("roomId") Long roomId){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "채팅방의 메세지가 조회되었습니다.", chatRoomService.viewChatMessageList(roomId));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 채팅방 퇴장
    @GetMapping("/exit")
    public ResponseEntity<?> chatRoomExit(){
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "채팅방을 나갑니다.", chatRoomService.exitChatRoom(userNum));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 채팅방 삭제
    @GetMapping("/hardExit/{roomId}")
    public ResponseEntity<?> chatRoomHardExit(@PathVariable("roomId") Long roomId){
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "채팅방을 삭제합니다.", chatRoomService.hardExitChatRoom(userNum, roomId));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 채팅방 구성원 조회
    @GetMapping("/chatUsers/{roomId}")
    public ResponseEntity<?> chatRoomChatUsers(@PathVariable("roomId") Long roomId){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "채팅방 유저를 조회합니다.", chatRoomService.viewChatUserList(roomId));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 채팅방 구성원 초대
    @PostMapping("/invite")
    public ResponseEntity<?> chatRoomInviteUser(@RequestBody ChatUserRequest dto){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "채팅방에 유저를 초대했습니다.", chatRoomService.inviteChatUser(dto.getInviteUserNum(), dto.getRoomId()));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

}
