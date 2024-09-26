package com.example.exodia.video.dto;

import com.example.exodia.user.domain.User;
import com.example.exodia.video.domain.VideoRoom;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class VideoRoomRedisDto {

    private Long id;
    private String roomName;
    private String password;
    private String hostName;
    private Boolean isActive;
    private int participantCount;
    private String createdAt;

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static VideoRoomRedisDto fromEntity(VideoRoom videoRoom) {
        VideoRoomRedisDto dto = new VideoRoomRedisDto();
        dto.setId(videoRoom.getId());
        dto.setRoomName(videoRoom.getRoomName());
        dto.setPassword(videoRoom.getPassword());
        dto.setHostName(videoRoom.getHost().getName());
        dto.setIsActive(videoRoom.getIsActive());
        dto.setParticipantCount(videoRoom.getParticipantCount());
        dto.setCreatedAt(videoRoom.getCreatedAt().format(formatter));
        return dto;
    }

    public VideoRoom toEntity(User host) {
        return new VideoRoom(
                this.id,
                this.roomName,
                this.password,
                host,
                this.isActive,
                this.participantCount,
                LocalDateTime.parse(this.createdAt, formatter)
        );
    }
}
