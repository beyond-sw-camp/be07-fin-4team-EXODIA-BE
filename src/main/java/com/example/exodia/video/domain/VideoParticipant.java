package com.example.exodia.video.domain;

import com.example.exodia.user.domain.User;
import lombok.*;
import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private VideoRoom room;

    @ManyToOne
    @JoinColumn(name = "user_num", nullable = false)
    private User user;
}
