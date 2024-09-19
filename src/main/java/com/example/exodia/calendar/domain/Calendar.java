package com.example.exodia.calendar.domain;

import com.example.exodia.calendar.dto.CalendarSaveDto;
import com.example.exodia.user.domain.User;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "del_yn = 'N'")
public class Calendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String content;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private String type;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 사번

//    public static Calendar fromDto(CalendarSaveDto dto, User user) {
//        return Calendar.builder()
//                .title(dto.getTitle())
//                .content(dto.getContent())
//                .startTime(dto.getStartTime())
//                .endTime(dto.getEndTime())
//                .type(dto.getType())
//                .user(user)
//                .build();
//    }
}
