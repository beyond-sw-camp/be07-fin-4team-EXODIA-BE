package com.example.exodia.user.controller;

import com.example.exodia.common.auth.JwtTokenProvider;
import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.common.service.UploadAwsFileService;
import com.example.exodia.user.dto.*;
import com.example.exodia.user.domain.User;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("user")
public class UserController {


    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UploadAwsFileService uploadAwsFileService;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider, UploadAwsFileService uploadAwsFileService) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.uploadAwsFileService = uploadAwsFileService;
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
    public ResponseEntity<?> registerUser(
            @ModelAttribute UserRegisterDto registerDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestHeader("Authorization") String token) {
        String departmentId = jwtTokenProvider.getDepartmentIdFromToken(token.substring(7));
        User newUser = userService.registerUser(registerDto, profileImage, departmentId);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "유저 등록 성공", newUser));
    }


    @PutMapping("/list/{userNum}")
    public ResponseEntity<?> updateUser(
            @PathVariable String userNum,
            @ModelAttribute UserUpdateDto updateDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestHeader("Authorization") String token) {

        String departmentId = jwtTokenProvider.getDepartmentIdFromToken(token.substring(7));

        String uploadedFilePath = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            uploadedFilePath = uploadAwsFileService.uploadFileAndReturnPath(profileImage, "profile");
        }

        User updatedUser = userService.updateUser(userNum, updateDto, departmentId, uploadedFilePath);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "유저 정보 수정 완료", updatedUser));
    }





    @GetMapping("/list")
    public ResponseEntity<List<UserInfoDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/list/{userNum}")
    public ResponseEntity<UserDetailDto> getUserDetail(@PathVariable String userNum) {
        return ResponseEntity.ok(userService.getUserDetail(userNum));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(
            @RequestBody UserDeleteDto userDeleteDto,
            @RequestHeader("Authorization") String token) {
        try {
            String deletedBy = jwtTokenProvider.getUserNumFromToken(token.substring(7));
            userService.deleteUser(userDeleteDto, deletedBy);
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
    @GetMapping("/department-users/{departmentId}")
    public ResponseEntity<List<UserInfoDto>> getUsersByDepartment(@PathVariable Long departmentId) {
        List<UserInfoDto> users = userService.getUsersByDepartment(departmentId);
        return ResponseEntity.ok(users); // 리스트를 HTTP 200 OK와 함께 반환
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserInfoDto>> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String searchType,
            Pageable pageable) {
        List<User> users = userService.searchUsers(search, searchType, pageable);
        List<UserInfoDto> userDtos = users.stream()
                .map(UserInfoDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/userName")
    public ResponseEntity<?> getUserName() {
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "이름 조회 성공", userService.getUserName()));
    }




}
