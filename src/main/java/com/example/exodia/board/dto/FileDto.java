package com.example.exodia.board.dto;

import com.example.exodia.board.domain.BoardFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileDto {
    private Long id;
    private String fileName;
    private String fileType;
    private String filePath;
    private Long fileSize;

    // BoardFile 단일 객체를 FileDto로 변환하는 정적 메서드 (빌더 패턴 사용)
    public static FileDto convertFilesToDto(BoardFile boardFile) {
        return FileDto.builder()
                .id(boardFile.getId())
                .fileName(boardFile.getFileName())
                .fileType(boardFile.getFileType())
                .filePath(boardFile.getFilePath())
                .fileSize(boardFile.getFileSize())
                .build();
    }

    // List<BoardFile>을 List<FileDto>로 변환하는 정적 메서드
    public static List<FileDto> convertFileListToDto(List<BoardFile> boardFiles) {
        return boardFiles != null ? boardFiles.stream()
                .map(FileDto::convertFilesToDto) // 각각의 BoardFile 객체를 FileDto로 변환
                .collect(Collectors.toList()) : List.of(); // 리스트가 null인 경우 빈 리스트 반환
    }
}
