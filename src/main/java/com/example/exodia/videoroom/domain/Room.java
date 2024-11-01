package com.example.exodia.videoroom.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomName;

    private String password;

//    @Column(nullable = false)
//    private int participantCount;

    @Column(nullable = false, unique = true)
    private String sessionId;

//    public void incrementParticipant() {
//        this.participantCount += 1;
//    }
//
//    public void decrementParticipant() {
//        this.participantCount -= 1;
//    }
}
