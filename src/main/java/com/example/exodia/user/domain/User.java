package com.example.exodia.user.domain;

import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.department.domain.Department;
import com.example.exodia.position.domain.Position;
import com.example.exodia.user.dto.UserRegisterDto;
import com.example.exodia.user.dto.UserUpdateDto;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_num", nullable = false, length = 12, unique = true)
    private String userNum;

    private String profileImage;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 100)
    private String address;

    @Column(nullable = false, length = 11)
    private String phone;

    @Column(nullable = false, length = 20)
    private String socialNum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HireType hireType;

    @Column(length = 100)
    private NowStatus n_status;

    @Column(nullable = false)
    private int annualLeave;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(nullable = false)
    private int loginFailCount = 0;

    public void incrementLoginFailCount() {
        this.loginFailCount += 1;
    }

    public void resetLoginFailCount() {
        this.loginFailCount = 0;
    }

    public void updateFromDto(UserUpdateDto dto, Department department, Position position) {
        this.name = dto.getName();
        this.email = dto.getEmail();
        this.phone = dto.getPhone();
        this.address = dto.getAddress();
        this.hireType = dto.getHireType();
        this.annualLeave = dto.getAnnualLeave();
        this.department = department;
        this.position = position;
    }

    public static User fromRegisterDto(UserRegisterDto dto, Department department, Position position, String encodedPassword) {
        return User.builder()
                .userNum(dto.getUserNum())
                .name(dto.getName())
                .gender(Gender.valueOf(dto.getGender()))
                .password(encodedPassword)
                .email(dto.getEmail())
                .address(dto.getAddress())
                .phone(dto.getPhone())
                .socialNum(dto.getSocialNum())
                .hireType(dto.getHireType())
                .annualLeave(dto.getAnnualLeave())
                .department(department)
                .position(position)
                .build();
    }


    public void softDelete() {
        super.softDelete();
    }

    // 테스트용 생성자
    public User(String userNum) {
        this.userNum = userNum;
    }

    public User(String name, Department department) {
        this.name = name;
        this.department = department;
    }
}
