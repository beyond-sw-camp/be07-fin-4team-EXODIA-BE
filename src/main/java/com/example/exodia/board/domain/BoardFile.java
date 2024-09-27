package com.example.exodia.board.domain;

import com.example.exodia.qna.domain.QnA;
import lombok.*;

import javax.persistence.*;

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
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne
    @JoinColumn(name = "qna_id")
    private QnA qna;

    @Column(nullable = false, length = 2083)
    private String filePath;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private Long fileSize;




    public static BoardFile createBoardFile(Board board, String filePath, String fileType, String fileName, Long fileSize) {
        return BoardFile.builder()
                .board(board)
                .filePath(filePath)
                .fileType(fileType)
                .fileName(fileName)
                .fileSize(fileSize)
                .build();
    }

    public static BoardFile createQnAFile(QnA qna, String filePath, String fileType, String fileName, Long fileSize) {
        return BoardFile.builder()
                .qna(qna)
                .filePath(filePath)
                .fileType(fileType)
                .fileName(fileName)
                .fileSize(fileSize)
                .build();
    }

}
