package com.example.exodia.user.dto;

import com.example.exodia.user.domain.*;
import com.example.exodia.department.domain.Department;
import com.example.exodia.position.domain.Position;
import com.example.exodia.department.repository.DepartmentRepository;
import com.example.exodia.position.repository.PositionRepository;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDto {
    private String userNum;
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
    private MultipartFile profileImage;
}
