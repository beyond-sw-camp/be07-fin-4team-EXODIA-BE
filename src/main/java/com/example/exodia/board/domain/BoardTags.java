package com.example.exodia.board.domain;


import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tags")
public class BoardTags {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tag;

}
