package com.example.exodia.calendar.dto;

import com.example.exodia.calendar.domain.Calendar;
import com.example.exodia.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
public class CalendarSaveDto {
    private String title;
    private String content;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String type;
    private Long userId;

    public Calendar toEntity(User user) {
        return Calendar.builder()
                .title(this.title)
                .content(this.content)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .type(this.type)
                .user(user)
                .build();
    }
}
