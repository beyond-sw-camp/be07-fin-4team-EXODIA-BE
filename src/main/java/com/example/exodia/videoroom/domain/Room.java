package com.example.exodia.videoroom.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String sessionId;  // OpenVidu 세션 ID

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants;

    @Column(nullable = false)
    private int participantCount = 0;

    // 참가자 추가
    public void addParticipant(Participant participant) {
        participants.add(participant);
        participant.setRoom(this);
        participantCount = participants.size();
    }

    // 참가자 제거
    public void removeParticipant(Participant participant) {
        participants.remove(participant);
        participantCount = participants.size();
    }
}
