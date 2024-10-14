package com.example.exodia.user.service;

import com.example.exodia.common.auth.JwtTokenProvider;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.common.service.UploadAwsFileService;
import com.example.exodia.department.domain.Department;
import com.example.exodia.department.repository.DepartmentRepository;
import com.example.exodia.position.domain.Position;
import com.example.exodia.position.repository.PositionRepository;
import com.example.exodia.user.domain.*;
import com.example.exodia.user.dto.*;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.userDelete.domain.DeleteHistory;
import com.example.exodia.userDelete.repository.DeleteHistoryRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DeleteHistoryRepository deleteHistoryRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UploadAwsFileService uploadAwsFileService;

    public UserService(UserRepository userRepository, DeleteHistoryRepository deleteHistoryRepository, DepartmentRepository departmentRepository, PositionRepository positionRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder, UploadAwsFileService uploadAwsFileService) {
        this.userRepository = userRepository;
        this.deleteHistoryRepository = deleteHistoryRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.uploadAwsFileService = uploadAwsFileService;
    }

    public String login(UserLoginDto loginDto) {
        User user = userRepository.findByUserNumAndDelYn(loginDto.getUserNum(), DelYN.N)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));
        if (user.isDeleted()) {
            throw new RuntimeException("비활성화 상태의 계정입니다.");
        }
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            user.incrementLoginFailCount();
            if (user.getLoginFailCount() >= 5) {
                user.softDelete();
            }
            userRepository.save(user);
            throw new RuntimeException("잘못된 이메일/비밀번호 입니다.");
        }
        user.resetLoginFailCount();
        userRepository.save(user);
        return jwtTokenProvider.createToken(user.getUserNum(),
                user.getDepartment().getId(),
                user.getPosition().getId());
    }


    @Transactional
    public User registerUser(UserRegisterDto registerDto, MultipartFile profileImage, String departmentId) {
        Department department = departmentRepository.findById(registerDto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 부서입니다."));
        Position position = positionRepository.findById(registerDto.getPositionId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 직급입니다."));

        String encodedPassword = passwordEncoder.encode(registerDto.getPassword());
        User newUser = new User();
        newUser.setName(registerDto.getName());
        newUser.setDepartment(department);
        newUser.setPosition(position);
        newUser.setPassword(encodedPassword);
        if (profileImage != null && !profileImage.isEmpty()) {
            String s3ImagePath = uploadAwsFileService.uploadFileAndReturnPath(profileImage, "profile");
            newUser.setProfileImage(s3ImagePath);
        }

        return userRepository.save(newUser);
    }

    @Transactional
    public User updateUser(String userNum, UserUpdateDto updateDto, String departmentId, String uploadedFilePath) {
        Department department = departmentRepository.findById(updateDto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 부서입니다."));
        Position position = positionRepository.findById(updateDto.getPositionId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 직급입니다."));

        User user = userRepository.findByUserNumAndDelYn(userNum, DelYN.N)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        if (updateDto.getPassword() != null && !updateDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }
        if (uploadedFilePath != null) {
            user.setProfileImage(uploadedFilePath);
        }
        user.updateFromDto(updateDto, department, position);
        return userRepository.save(user);
    }



    public void checkHrAuthority(String departmentId) {
        System.out.println("Received departmentId: " + departmentId);
        Department hrDepartment = departmentRepository.findById(Long.parseLong(departmentId))
                .orElseThrow(() -> new RuntimeException("해당 부서가 존재하지 않습니다."));

        if (!hrDepartment.getName().equals("인사팀")) {
            System.out.println("Received departmentId: " + departmentId);
            throw new RuntimeException("권한이 없습니다. 인사팀만 이 작업을 수행할 수 있습니다.");
        }
    }




    public List<UserInfoDto> getAllUsers() {
        List<User> users = userRepository.findAllByDelYn(DelYN.N);
        return users.stream()
                .map(UserInfoDto::fromEntity)
                .collect(Collectors.toList());
    }

    public UserDetailDto getUserDetail(String userNum) {
        User user = userRepository.findByUserNumAndDelYn(userNum, DelYN.N)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));
        return UserDetailDto.fromEntity(user);
    }


    @Transactional
    public void deleteUser(UserDeleteDto deleteDto, String deletedBy) {
        // 삭제 대상자 찾기
        User user = userRepository.findByUserNumAndDelYn(deleteDto.getUserNum(), DelYN.N)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        user.softDelete();
        userRepository.save(user);

        DeleteHistory deleteHistory = new DeleteHistory(deletedBy, deleteDto.getReason(), user);
        deleteHistoryRepository.save(deleteHistory);
    }


    public UserProfileDto getUserProfile(String userNum) {
        User user = userRepository.findByUserNumAndDelYn(userNum, DelYN.N)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        return UserProfileDto.fromProfileEntity(user);
    }

    @Transactional(readOnly = true)
    public List<UserInfoDto> getUsersByDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("해당 부서가 존재하지 않습니다."));

        List<User> users = userRepository.findAllByDepartmentId(departmentId);
        return users.stream()
                .map(UserInfoDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<User> searchUsers(String search, String searchType, Pageable pageable) {
        if (search == null || search.isEmpty()) {
            return userRepository.findByDelYn(DelYN.N, pageable).getContent();
        }

        switch (searchType) {
            case "name":
                return userRepository.findByNameContainingAndDelYn(search, DelYN.N, pageable).getContent();
            case "department":
                return userRepository.findByDepartmentNameContainingAndDelYn(search, DelYN.N, pageable).getContent();
            case "position":
                return userRepository.findByPositionNameContainingAndDelYn(search, DelYN.N, pageable).getContent();
            case "all":
                return userRepository.findByNameContainingOrDepartmentNameContainingOrPositionNameContainingAndDelYn(
                        search, search, search, DelYN.N, pageable).getContent();
            default:
                return userRepository.findByDelYn(DelYN.N, pageable).getContent();
        }

    }
}
