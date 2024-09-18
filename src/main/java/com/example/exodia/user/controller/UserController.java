package com.example.exodia.user.controller;

import com.example.exodia.common.auth.JwtTokenProvider;
import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.dto.*;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {


    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto loginDto) {
        try {
            String token = userService.login(loginDto);
            System.out.println("Generated JWT Token: " + token);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "로그인 성공", token);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (e.getMessage().contains("비활성화 상태")) {
                CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.UNAUTHORIZED, e.getMessage());
                return new ResponseEntity<>(commonErrorDto, HttpStatus.UNAUTHORIZED);
            }
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.UNAUTHORIZED, e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.UNAUTHORIZED, "로그인 중 오류가 발생했습니다.");
            return new ResponseEntity<>(commonErrorDto, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDto registerDto, @RequestHeader("Authorization") String token) {
        String departmentName = jwtTokenProvider.getDepartmentNameFromToken(token.substring(7));
        User newUser = userService.registerUser(registerDto, departmentName);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "유저 등록 성공", newUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserUpdateDto updateDto, @RequestHeader("Authorization") String token) {
        String departmentName = jwtTokenProvider.getDepartmentNameFromToken(token.substring(7));
        User updatedUser = userService.updateUser(id, updateDto, departmentName);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "유저 정보 수정 완료", updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id, @RequestParam String deletedBy, @RequestParam String reason, @RequestHeader("Authorization") String token) {
        String departmentName = jwtTokenProvider.getDepartmentNameFromToken(token.substring(7));
        userService.deleteUser(id, deletedBy, reason, departmentName);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "유저 삭제 완료", null));
    }

    @GetMapping
    public ResponseEntity<List<UserInfoDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailDto> getUserDetail(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserDetail(id));
    }


//    @DeleteMapping("/delete")
//    public ResponseEntity<?> deleteUser(@RequestBody UserDeleteDto userDeleteDto, @RequestParam String departmentName) {
//        userService.deleteUser(userDeleteDto, departmentName);
//        CommonResDto response = new CommonResDto(HttpStatus.OK, "유저 삭제 성공", null);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
}
