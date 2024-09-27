package com.example.exodia.meetingRoom.domain;

import com.example.exodia.meetingRoom.dto.MeetingRoomUpdateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    
    
    // 단위 테스트를 위해서
    public MeetingRoom(String name) {
        this.name = name;
    }
}
