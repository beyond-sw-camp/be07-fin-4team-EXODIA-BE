package com.example.exodia.user.controller;

import com.example.exodia.common.auth.JwtTokenProvider;
import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import com.example.exodia.common.service.UploadAwsFileService;
import com.example.exodia.submit.dto.PasswordChangeDto;
import com.example.exodia.user.dto.*;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
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
            @ModelAttribute("user") UserRegisterDto registerDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestHeader("Authorization") String token) {
        String departmentId = jwtTokenProvider.getDepartmentIdFromToken(token.substring(7));
        try {
            User newUser = userService.registerUser(registerDto, profileImage, departmentId);
            return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "유저 등록 성공", newUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new CommonResDto(HttpStatus.BAD_REQUEST, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "유저 등록 중 오류 발생", null));
        }
    }

    @PutMapping("/list/{userNum}")
    public ResponseEntity<?> updateUser(
            @PathVariable String userNum,
            @ModelAttribute UserUpdateDto updateDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestHeader("Authorization") String token) {

        String departmentId = jwtTokenProvider.getDepartmentIdFromToken(token.substring(7));
        String uploadedFilePath = updateDto.getProfileImageUrl();

        if (profileImage != null && !profileImage.isEmpty()) {
            uploadedFilePath = uploadAwsFileService.uploadFileAndReturnPath(profileImage, "profile");
        }
        User updatedUser = userService.updateUser(userNum, updateDto, departmentId, uploadedFilePath);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "유저 정보 수정 완료", updatedUser));
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<User> userPage = userService.getAllUsers(page, size);

        List<UserInfoDto> users = userPage.getContent().stream()
                .map(UserInfoDto::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("currentPage", userPage.getNumber());
        response.put("totalItems", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
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
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String searchType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> users = userService.searchUsers(search, searchType, page, size);
        List<UserInfoDto> userDtos = users.stream()
                .map(UserInfoDto::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("users", userDtos);
        response.put("currentPage", users.getNumber());
        response.put("totalItems", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
  
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody UserLoginDto loginDto) {
        try {
            Long positionId = userService.findPositionIdByUserNum(loginDto.getUserNum());
            Long departmentId = userService.findDepartmentIdByUserNum(loginDto.getUserNum());
            String newToken = jwtTokenProvider.createToken(loginDto.getUserNum(), departmentId, positionId);
            return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "토큰 재발급 성공", newToken), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED, "토큰 재발급 실패"), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/userName")
    public ResponseEntity<?> getUserName() {
          return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "이름 조회 성공", userService.getUserName()));
    }



    @GetMapping("/department/{departmentId}/search")
    public ResponseEntity<List<UserInfoDto>> searchUsersInDepartment(
            @PathVariable Long departmentId,
            @RequestParam(required = false) String searchQuery) {
        List<User> users = userService.searchUsersInDepartment(departmentId, searchQuery);
        List<UserInfoDto> userDtos = users.stream()
                .map(UserInfoDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }


    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeDto passwordChangeDto, @RequestHeader("Authorization") String token) {
        try {
            String userNum = jwtTokenProvider.getUserNumFromToken(token.substring(7));
            userService.changePassword(userNum, passwordChangeDto);
            return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "비밀번호 변경 성공", null));
        } catch (Exception e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED, e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/generateUserNum/{date}")
    public ResponseEntity<Map<String, String>> generateUserNum(@PathVariable String date) {
        String newUserNum = userService.generateUserNum(date);
        Map<String, String> response = new HashMap<>();
        response.put("userNum", newUserNum);
        return ResponseEntity.ok(response);
    }


}
