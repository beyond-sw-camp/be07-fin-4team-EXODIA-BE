package com.example.exodia.board.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "board_file")
public class BoardFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(nullable = false, length = 2083)
    private String filePath;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = true, length = 2083)
    private String fileDownloadUrl;


    public static BoardFile createBoardFile(Board board, String filePath, String fileType, String fileName, Long fileSize, String fileDownloadUrl) {
        return BoardFile.builder()
                .board(board)
                .filePath(filePath)
                .fileType(fileType)
                .fileName(fileName)
                .fileSize(fileSize)
                .fileDownloadUrl(fileDownloadUrl)
                .build();
    }
}
