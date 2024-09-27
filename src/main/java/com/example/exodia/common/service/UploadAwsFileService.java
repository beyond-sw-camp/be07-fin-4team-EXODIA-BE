package com.example.exodia.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class UploadAwsFileService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Client s3Client;
    private final CommonMethod commonMethod;

    @Autowired
    public UploadAwsFileService(S3Client s3Client, CommonMethod commonMethod) {
        this.s3Client = s3Client;
        this.commonMethod = commonMethod;
    }

    // 다중 파일 업로드 메서드
    public List<String> uploadMultipleFilesAndReturnPaths(List<MultipartFile> files, String folder) {
        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) {
                    System.out.println("빈 파일이므로 업로드 건너뜁니다. 파일 이름: " + file.getOriginalFilename());
                    continue;
                }

                String originalFileName = file.getOriginalFilename();
                String fileName = getUniqueFileName(originalFileName);

                byte[] fileData = file.getBytes();

                if (!commonMethod.fileSizeCheck(file)) {
                    throw new IllegalArgumentException("파일의 크기가 너무 큽니다: " + fileName);
                }

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(folder+"/"+fileName)
                        .contentType(file.getContentType())
                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileData));

                String s3FilePath = s3Client.utilities()
                        .getUrl(a -> a.bucket(bucket).key(fileName))
                        .toExternalForm();

                fileUrls.add(s3FilePath);

            } catch (IOException e) {
                throw new RuntimeException("파일 업로드 중 오류 발생: " + (file != null ? file.getOriginalFilename() : "null"), e);
            } catch (Exception e) {
                throw new RuntimeException("파일 업로드 중 알 수 없는 오류 발생: " + (file != null ? file.getOriginalFilename() : "null"), e);
            }
        }

        return fileUrls;
    }

    // S3에서 파일 이름이 중복되는지 확인하고 중복 시 파일 이름에 숫자를 붙임
    private String getUniqueFileName(String originalFileName) {
        String baseName = getBaseName(originalFileName);
        String extension = getExtension(originalFileName);

        String fileName = baseName + extension;
        int count = 1;

        // 파일이 존재하는지 확인, 중복이면 숫자 추가
        while (doesFileExistInS3(fileName)) {
            fileName = baseName + " (" + count + ")" + extension;
            count++;
        }

        return fileName;
    }

    // 파일이 S3에 존재하는지 확인
    private boolean doesFileExistInS3(String fileName) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build());
            return true; // 파일이 존재함
        } catch (NoSuchKeyException e) {
            return false; // 파일이 존재하지 않음
        }
    }

    // 파일 확장자를 추출하는 메서드
    private String getExtension(String originalFileName) {
        if (originalFileName != null && originalFileName.contains(".")) {
            return originalFileName.substring(originalFileName.lastIndexOf("."));
        } else {
            return "";
        }
    }

    // 파일 이름에서 확장자를 제외한 부분 추출
    private String getBaseName(String originalFileName) {
        if (originalFileName != null && originalFileName.contains(".")) {
            return originalFileName.substring(0, originalFileName.lastIndexOf("."));
        } else {
            return originalFileName; // 확장자가 없는 경우 원래 이름 반환
        }
    }
}