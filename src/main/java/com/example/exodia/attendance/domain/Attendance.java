package com.example.exodia.attendance.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "del_yn = 'N'")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime inTime;

    private LocalDateTime outTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayStatus dayStatus;

}
