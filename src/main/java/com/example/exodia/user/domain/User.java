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
//    @Column(nullable = false)
    //test 시에 false
    private HireType hireType;

    @Column(length = 100)
    private NowStatus n_status;

    @Column(nullable = false)
    private double annualLeave;

    @Column(nullable = false)
    private double sickDay = 0; // 사용 병가

    @Column(nullable = false)
    private double absentDay = 0; // 결근

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
        this.gender = Gender.valueOf(dto.getGender());
        this.status = Status.valueOf(dto.getStatus());
    }


    public static User fromRegisterDto(UserRegisterDto dto, Department department, Position position, Status status, String encodedPassword) {
        User user = new User();
        user.setUserNum(dto.getUserNum());
        user.setName(dto.getName());
        user.setPassword(encodedPassword);
        user.setEmail(dto.getEmail());
        user.setAddress(dto.getAddress());
        user.setPhone(dto.getPhone());
        user.setSocialNum(dto.getSocialNum());
        user.setGender(Gender.valueOf(dto.getGender()));
        user.setHireType(dto.getHireType());
        user.setStatus(status);
        user.setAnnualLeave(dto.getAnnualLeave());
        user.setDepartment(department);
        user.setPosition(position);
        return user;
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

    public void updateAnnualLeave(double vacationDate) {
        this.annualLeave -= vacationDate;
    }

    public void updateSickDay(double vacationDate) {
        this.sickDay += vacationDate;
    }

    public void setWorkIn(){
        this.n_status = NowStatus.출근;
    }

    public void setWorkOut(){
        this.n_status = NowStatus.퇴근;
    }

    public void setMeetingStatus(){
        this.n_status = NowStatus.자리비움;
    }
}
