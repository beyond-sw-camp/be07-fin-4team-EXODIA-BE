package com.example.exodia.sms.controller;

import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.sms.service.CoolSmsService;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    private final CoolSmsService coolSmsService;


    @Autowired
    public SmsController(CoolSmsService coolSmsService) {
        this.coolSmsService = coolSmsService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendSms(@RequestBody Map<String, String> body) {
        String phoneNumber = body.get("phoneNumber");

        try {
            String sentMessage = coolSmsService.sendSms(phoneNumber);
            CommonResDto response = new CommonResDto(HttpStatus.OK, "Message sent successfully", sentMessage);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (CoolsmsException e) {
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send SMS: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.BAD_REQUEST, "Invalid request data: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}
