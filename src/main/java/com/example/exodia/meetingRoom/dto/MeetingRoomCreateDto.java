package com.example.exodia.meetingRoom.dto;

import com.example.exodia.meetingRoom.domain.MeetingRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRoomCreateDto {
    private String name;

    public MeetingRoom toEntity() {
        return MeetingRoom.builder()
                .name(this.name)
                .build();
    }
}

