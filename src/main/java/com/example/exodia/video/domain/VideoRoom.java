package com.example.exodia.video.domain;

import com.example.exodia.user.domain.User;
import com.example.exodia.video.dto.VideoRoomRedisDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomName;

    @Column
    private String password;
    private Long janusRoomId;
    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private int participantCount = 0;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public void increaseParticipantCount() {
        this.participantCount += 1;
    }

    public void decreaseParticipantCount() {
        this.participantCount -= 1;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public VideoRoomRedisDto toDto() {
        return VideoRoomRedisDto.fromEntity(this);
    }

    public VideoRoom(Long janusRoomId, String roomName, String password, User host, Boolean isActive, int participantCount, LocalDateTime createdAt) {
        this.janusRoomId = janusRoomId;
        this.roomName = roomName;
        this.password = password;
        this.host = host;
        this.isActive = isActive;
        this.participantCount = participantCount;
        this.createdAt = createdAt;
    }
}
