package com.example.exodia.user.dto;

import com.example.exodia.user.domain.*;
import com.example.exodia.department.domain.Department;
import com.example.exodia.position.domain.Position;
import com.example.exodia.department.repository.DepartmentRepository;
import com.example.exodia.position.repository.PositionRepository;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
public class UserRegisterDto {
    private String id;
    private String profileImage;
    private String name;
    private String gender;
    private String status;
    private String password;
    private String email;
    private String address;
    private String phone;
    private String socialNum;
    private HireType hireType;
    private NowStatus nowStatus;
    private Long departmentId;
    private Long positionId;
    private int annualLeave;

    public User toEntity(DepartmentRepository departmentRepository,
                         PositionRepository positionRepository,
                         PasswordEncoder passwordEncoder) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 부서입니다."));
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 직급입니다."));

        return new User(
                null, null, this.name, null, Status.재직,
                passwordEncoder.encode(this.password),
                this.email, null, this.phone, null,
                this.socialNum, this.hireType, null,
                this.annualLeave, department, position, 0);
    }
}
