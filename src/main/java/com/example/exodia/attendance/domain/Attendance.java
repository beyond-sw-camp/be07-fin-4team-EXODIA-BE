package com.example.exodia.attendance.domain;

import com.example.exodia.user.domain.NowStatus;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.dto.UserStatusAndTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;


@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
//@Where(clause = "del_yn = 'N'")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime inTime; // 출근 시간

    private LocalDateTime outTime; // 퇴근 시간

    // @Enumerated(EnumType.STRING)
    // @Column(nullable = false)
    // private DayStatus dayStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NowStatus nowStatus;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public double getHoursWorked() {
        if (inTime != null && outTime != null) {
            // inTime과 outTime 사이의 시간을 계산 (초 단위)
            Duration duration = Duration.between(inTime, outTime);
            return duration.toHours() + (duration.toMinutesPart() / 60.0); // 시간을 소수점 단위로 반환
        }
        return 0.0;
    }

    public UserStatusAndTime fromEntity(User user) {
        LocalDateTime in = null;
        LocalDateTime out = null;

        if(user.getN_status() == NowStatus.출근){
            in = this.getInTime();
        }else if(user.getN_status() == NowStatus.퇴근) {
            in = this.getInTime();
            out = this.getOutTime();
        }
        return UserStatusAndTime.builder()
            .userName(this.user.getName())
            .profileImage(this.user.getProfileImage())
            .nowStatus(user.getN_status())
            .inTime(in)
            .outTime(out)
            .build();
    }

    public void setMeetingStatus(){
        this.nowStatus = NowStatus.자리비움;
    }

    public void setWorkIn(){
        this.nowStatus = NowStatus.출근;
    }
}
