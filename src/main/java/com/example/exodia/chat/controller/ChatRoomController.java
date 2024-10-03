package com.example.exodia.chat.controller;

import com.example.exodia.chat.dto.ChatRoomRequest;
import com.example.exodia.chat.service.ChatRoomService;
import com.example.exodia.common.dto.CommonResDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/create")
    public ResponseEntity<?> chatRoomCreate(@RequestBody ChatRoomRequest dto){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "채팅방이 생성되었습니다.", chatRoomService.createChatRoom(dto));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

//    @GetMapping("/list/{userNum}")
//    public ResponseEntity<?> chatRoomList(@PathVariable("userNum") String userNum){
//        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "채팅방목록이 조회되었습니다.", chatRoomService.viewChatRoomList(userNum));
//        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
//    }

    @GetMapping("/{roomId}")
    public ResponseEntity<?> chatRoomView(@PathVariable("roomId") Long roomId){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "채팅방이 조회되었습니다.", chatRoomService.viewChatMessageList(roomId));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

}
