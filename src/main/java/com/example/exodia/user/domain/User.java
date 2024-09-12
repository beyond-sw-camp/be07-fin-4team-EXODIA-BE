package com.example.exodia.user.domain;

import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.department.domain.Department;
import com.example.exodia.position.domain.Position;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Where(clause = "del_yn = 'N'")
public class User extends BaseTimeEntity {

    @Id
    @Column(nullable = false, length = 12)
    private String id;

    @Column(nullable = false)
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

    @Column(nullable = false, length = 11)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    private DelYN delYn = DelYN.N;

    @Column(nullable = false, length = 20)
    private String socialNum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HireType hire;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

}
