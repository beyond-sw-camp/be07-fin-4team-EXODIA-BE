package com.example.exodia.user.controller;

import com.example.exodia.common.auth.JwtTokenProvider;
import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.dto.*;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

//    @PostMapping("/register")
//    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDto registerDto, @RequestHeader("Authorization") String token) {
////        String departmentName = jwtTokenProvider.getDepartmentNameFromToken(token.substring(7));
//        String departmentid = jwtTokenProvider.getDepartmentIdFromToken(token.substring(7));
//        User newUser = userService.registerUser(registerDto, departmentid);
//        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "유저 등록 성공", newUser));
//    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestPart("user") UserRegisterDto registerDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestHeader("Authorization") String token) {
        String departmentId = jwtTokenProvider.getDepartmentIdFromToken(token.substring(7));
        User newUser = userService.registerUser(registerDto, profileImage, departmentId);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "유저 등록 성공", newUser));
    }


    @GetMapping("/list")
    public ResponseEntity<List<UserInfoDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/list/{userNum}")
    public ResponseEntity<UserDetailDto> getUserDetail(@PathVariable String userNum) {
        return ResponseEntity.ok(userService.getUserDetail(userNum));
    }

    @PutMapping("/list/{userNum}")
    public ResponseEntity<?> updateUser(
            @PathVariable String userNum,
            @RequestBody UserUpdateDto updateDto,
            @RequestHeader("Authorization") String token) {
        try {
            String departmentId = jwtTokenProvider.getDepartmentIdFromToken(token.substring(7));

            if (updateDto.getDepartmentId() == null || updateDto.getPositionId() == null) {
                return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, "부서 또는 직급 ID가 누락되었습니다."), HttpStatus.BAD_REQUEST);
            }

            User updatedUser = userService.updateUser(userNum, updateDto, departmentId);
            return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "유저 정보 수정 완료", updatedUser));

        } catch (RuntimeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED, e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }


   @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestBody UserDeleteDto userDeleteDto, @RequestHeader("Authorization") String token) {
        try {
            String departmentId = jwtTokenProvider.getDepartmentIdFromToken(token.substring(7));
            userService.deleteUser(userDeleteDto, departmentId);
            return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "유저 삭제 성공", null));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED, e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }


    // 프로필 이미지
    @GetMapping("/profile/{userNum}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable String userNum) {
        UserProfileDto userProfile = userService.getUserProfile(userNum);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam(required = false) String search, @RequestParam(required = false) String searchType, Pageable pageable
    ) {
        List<User> users = userService.searchUsers(search, searchType, pageable);
        return ResponseEntity.ok(users);

    }
}
