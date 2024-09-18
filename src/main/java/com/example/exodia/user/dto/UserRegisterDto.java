package com.example.exodia.user.dto;

import com.example.exodia.user.domain.User;
import com.example.exodia.department.domain.Department;
import com.example.exodia.position.domain.Position;
import com.example.exodia.department.repository.DepartmentRepository;
import com.example.exodia.position.repository.PositionRepository;
import com.example.exodia.user.domain.HireType;
import com.example.exodia.user.domain.Status;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
public class UserRegisterDto {

    private String name;
    private String email;
    private String phone;
    private String password;
    private String socialNum;
    private HireType hire;
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
                this.socialNum, this.hire, null,
                this.annualLeave, department, position, 0);
    }
}
