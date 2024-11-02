package com.example.exodia.user.service;

import com.example.exodia.attendance.domain.Attendance;
import com.example.exodia.common.auth.JwtTokenProvider;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.common.service.UploadAwsFileService;
import com.example.exodia.department.domain.Department;
import com.example.exodia.department.repository.DepartmentRepository;
import com.example.exodia.position.domain.Position;
import com.example.exodia.position.repository.PositionRepository;
import com.example.exodia.salary.service.SalaryService;
import com.example.exodia.submit.dto.PasswordChangeDto;
import com.example.exodia.user.domain.NowStatus;
import com.example.exodia.user.domain.Status;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.dto.*;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.userDelete.domain.DeleteHistory;
import com.example.exodia.userDelete.repository.DeleteHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.*;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    private final SalaryService salaryService;
    private final UserRepository userRepository;
    private final DeleteHistoryRepository deleteHistoryRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UploadAwsFileService uploadAwsFileService;

    public UserService(SalaryService salaryService, UserRepository userRepository, DeleteHistoryRepository deleteHistoryRepository, DepartmentRepository departmentRepository, PositionRepository positionRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder, UploadAwsFileService uploadAwsFileService) {
        this.salaryService = salaryService;
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
            if (user.getLoginFailCount() > 5) {
                user.softDelete();
            }
            user.resetLoginFailCount();
            userRepository.save(user);
            throw new RuntimeException("잘못된 이메일/비밀번호 입니다.");
        }
        Attendance.builder().inTime(null).outTime(null).nowStatus(NowStatus.근무전).user(user).build();

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

        if (registerDto.getPassword() == null || registerDto.getPassword().isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해야 합니다.");
        }
        if (registerDto.getAddress() == null || registerDto.getAddress().isEmpty()) {
            throw new IllegalArgumentException("주소를 입력해야 합니다.");
        }
        if (registerDto.getSocialNum() == null || registerDto.getSocialNum().isEmpty()) {
            throw new IllegalArgumentException("주민등록번호를 입력해야 합니다.");
        }

        String encodedPassword = passwordEncoder.encode(registerDto.getPassword());

        Status status = Status.valueOf(registerDto.getStatus());
        User newUser = User.fromRegisterDto(registerDto, department, position, status, encodedPassword);

        if (profileImage != null && !profileImage.isEmpty()) {
            String s3ImagePath = uploadAwsFileService.uploadFileAndReturnPath(profileImage, "profile");
            newUser.setProfileImage(s3ImagePath);
        }

        userRepository.save(newUser);
        salaryService.createSalaryForUser(newUser);

        return newUser;
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

    public Page<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAllByDelYn(DelYN.N, pageable);
    }


    public UserDetailDto getUserDetail(String userNum) {
        User user = userRepository.findByUserNumAndDelYn(userNum, DelYN.N)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));
        return UserDetailDto.fromEntity(user);
    }

    // user의 모든 chatUser에서 삭제, chatRoom마다 퇴장 메세지
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

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String userNum) {
        User user = userRepository.findByUserNumAndDelYn(userNum, DelYN.N)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        return UserProfileDto.fromProfileEntity(user);
    }

    @Transactional(readOnly = true)
    public List<UserInfoDto> getUsersByDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("해당 부서가 존재하지 않습니다."));

        List<User> users = userRepository.findAllByDepartmentIdAndDelYn(departmentId, DelYN.N);

        return users.stream()
                .map(UserInfoDto::fromEntity)
                .collect(Collectors.toList());
    }


    public Page<User> searchUsers(String search, String searchType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (search == null || search.isEmpty()) {
            return userRepository.findByDelYn(DelYN.N, pageable);
//            return userRepository.findByDelYn(DelYN.N, pageable).getContent();
        }

        switch (searchType) {
            case "name":
                return userRepository.findByNameContainingAndDelYn(search, DelYN.N, pageable);
            case "department":
                return userRepository.findByDepartmentNameContainingAndDelYn(search, DelYN.N, pageable);
            case "position":
                return userRepository.findByPositionNameContainingAndDelYn(search, DelYN.N, pageable);
            case "all":
                return userRepository.findByDelYnAndNameContainingOrDepartmentNameContainingOrPositionNameContaining(
                        DelYN.N, search, search, search, pageable);
            default:
                return userRepository.findByDelYn(DelYN.N, pageable);
        }
    }

    //  userNum으로 회원 이름 찾아오기
    public String getUserName() {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보가 존재하지 않습니다.")).getName();
    }

    public Long findPositionIdByUserNum(String userNum) {
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.getPosition().getId();
    }

    public Long findDepartmentIdByUserNum(String userNum) {
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.getDepartment().getId(); // 부서 ID 반환
    }

    /* 테스트 전용 */
    @Transactional
    public User createAndSaveTestUser(UserRegisterDto userRegisterDto, MultipartFile profileImage) {
        Department department = departmentRepository.findById(userRegisterDto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 부서입니다."));
        Position position = positionRepository.findById(userRegisterDto.getPositionId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 직급입니다."));

        String encodedPassword = passwordEncoder.encode(userRegisterDto.getPassword());

        Status status = Status.valueOf(userRegisterDto.getStatus());
        User newUser = User.fromRegisterDto(userRegisterDto, department, position, status, encodedPassword);

        if (profileImage != null && !profileImage.isEmpty()) {
            String s3ImagePath = uploadAwsFileService.uploadFileAndReturnPath(profileImage, "profile");
            newUser.setProfileImage(s3ImagePath);
        }
        return userRepository.save(newUser);
    }



    @Transactional
    public List<User> searchUsersInDepartment(Long departmentId, String searchQuery) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            return userRepository.findByDepartmentId(departmentId);
        } else {
            return userRepository.findByDepartmentIdAndNameContaining(departmentId, searchQuery);
        }
    }

    public void changePassword(String userNum, PasswordChangeDto passwordChangeDto) {
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
        userRepository.save(user);
    }

    public String generateUserNum(String date) {
        String lastUserNum = userRepository.findLastUserNum(date);
        if (lastUserNum == null) {
            return date + "001";
        }
        int lastNum = Integer.parseInt(lastUserNum.substring(8));
        String newUserNum = String.format("%03d", lastNum + 1);
        return date + newUserNum;
    }

}