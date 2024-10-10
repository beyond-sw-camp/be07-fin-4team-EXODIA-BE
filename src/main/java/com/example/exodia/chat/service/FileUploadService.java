package com.example.exodia.chat.service;

import com.example.exodia.chat.domain.ChatFile;
import com.example.exodia.chat.domain.ChatMessage;
import com.example.exodia.chat.dto.*;
import com.example.exodia.chat.repository.ChatFileRepository;
import com.example.exodia.chat.repository.ChatMessageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileUploadService {

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "sh", "msi", "dll", "vbs"
    );

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Client s3Client; // 파일 삭제시 필요
    private final S3Presigner s3Presigner;
    private final ChatFileRepository chatFileRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public FileUploadService(S3Client s3Client, S3Presigner s3Presigner, ChatFileRepository chatFileRepository, ChatMessageRepository chatMessageRepository) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.chatFileRepository = chatFileRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    // 다중 파일에 대한 Presigned URL 생성
    public Map<String, String> generatePresignedUrls(List<ChatFileRequest> files) {
        return files.stream().collect(Collectors.toMap(
                ChatFileRequest::getChatFileName,
                this::generatePresignedUrlAfterValidation
        ));
    }

    // 파일 검증 후  url 생성
    private String generatePresignedUrlAfterValidation(ChatFileRequest file) {
        validateFile(file.getFileSize(), file.getChatFileName());
        String uniqueFileName = generateUniqueFileName(file.getChatFileName()); // UUID가 포함된 고유한 파일 이름 생성
        return generatePresignedUrl(uniqueFileName);
    }

    // 파일 검증
    private void validateFile(long fileSize, String fileName) {
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다: " + fileName);
        }
        String fileExtension = getFileExtension(fileName).toLowerCase();
        if (BLOCKED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("이 파일 형식은 업로드할 수 없습니다: " + fileExtension);
        }
    }

    // 파일 확장자 추출 메서드
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex + 1);
    }

    // 고유한 파일 이름을 생성하는 메서드 (UUID + 파일 확장자, URL이 너무 길어서) -> s3에 저장될 이름.
    private String generateUniqueFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        String extension = "";
        // 파일 확장자 추출
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex != -1) {
            extension = originalFileName.substring(dotIndex);  // 확장자 포함
        }
        // 파일 이름을 UUID + 확장자로 축약하여 생성
        return uuid + extension;
    }

    // 단일 파일 Presigned URL 생성
    public String generatePresignedUrl(String fileName) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)  // UUID가 포함된 고유한 파일 이름 사용
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignPutObjectRequest ->
                presignPutObjectRequest
                        .signatureDuration(Duration.ofMinutes(10)) // 10분 유효
                        .putObjectRequest(putObjectRequest)
        );

        return presignedRequest.url().toString();
    }

    // 파일 메타데이터 DB 저장 (프론트엔드로부터 Presigned URL -> S3 URL 저장 -> 다운로드)
    @Transactional // 메세지 전송할 때 TYPE FILE일 경우 저장.
    public List<ChatFileMetaDataResponse> saveChatFileMetaData(ChatMessage messasge, List<ChatFileSaveListDto> chatFileSaveListDtos){
        // 파일을 포함하는 chatMesssage 전송되었을때, message db에 저장하고 -> message 넘겨서 파일도 db에 저장

        if(chatFileSaveListDtos == null){
            throw new IllegalArgumentException("파일 메타데이터가 필요합니다.");
        }

        // file DB 저장.
        List<ChatFile> chatFiles = chatFileSaveListDtos.stream()
                .map(dto -> createChatFile(dto, messasge)).collect(Collectors.toList());
        List<ChatFile> savedChatFiles = chatFileRepository.saveAll(chatFiles);

        return savedChatFiles.stream()
                .map(ChatFileMetaDataResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ChatFileSaveListDto 에서 ChatFile 생성
    private ChatFile createChatFile(ChatFileSaveListDto fileSaveListDto, ChatMessage chatMessage) {
        return ChatFile.builder()
                .chatMessage(chatMessage)
                .chatFileName(fileSaveListDto.getChatFileName()) // 원본 파일 이름 저장
                .chatFileUrl(fileSaveListDto.getChatFileUrl()) // 프론트에서 전달된 URL을 데이터베이스에 저장
                .build();
    }

    // file 다운로드
    @Transactional(readOnly = true)
    public String getPresignedUrlToDownload(Long fileId) {
        ChatFile chatFile = chatFileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("파일이 없습니다."));

//        파일 다운로드 권한 검증 // 채팅방 마다 파일을 불러올건데 굳이..? 사실 보안을 위해서는 필요.. 파일 다운받으려는 유저와가 채팅방유저들 중 있으면 ok
//        boolean hasPermission = chatFile.getFolder().getChannel().getChannelMembers()
//                .stream().anyMatch(channelMember -> channelMember.getWorkspaceMember().getMember().equals(member));

//        if (!hasPermission) {
//            throw new IllegalArgumentException("파일을 다운로드할 권한이 없습니다.");
//        }

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(b -> b.getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(chatFile.getChatFileUrl().substring(chatFile.getChatFileUrl().lastIndexOf('/') + 1))
                        .build())
                .signatureDuration(Duration.ofMinutes(1)));

        // Presigned URL 생성
        return presignedRequest.url().toString();
//        // Presigned URL 생성
//        try {
//            URI presignedUrl = s3Presigner.presignGetObject(b -> b.getObjectRequest(getObjectRequest)
//                            .signatureDuration(Duration.ofMinutes(1)))
//                    .url().toURI();
//            return presignedUrl.toString(); // 클라이언트에 반환
//        }catch (Exception e){
//            throw new IllegalArgumentException("Presigned URL 생성에 실패했습니다.");
//        }
    }

}
