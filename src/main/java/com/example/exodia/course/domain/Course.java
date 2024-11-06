package com.example.exodia.course.domain;

import com.example.exodia.common.domain.BaseTimeEntity;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Course extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String courseName; // 이벤트 명

    @Column(nullable = false)
    private String content; // 이벤트

    private String courseUrl; // string 값

    @Column(nullable = false)
    private LocalDateTime startTime; // 예약 시작 시간

    private int maxParticipants; // 최대 참가자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_num", nullable = false)
    private User user;

    @Column(name = "transmitted", nullable = false)
    private boolean transmitted = false; // 강좌의 전송 여부
}
