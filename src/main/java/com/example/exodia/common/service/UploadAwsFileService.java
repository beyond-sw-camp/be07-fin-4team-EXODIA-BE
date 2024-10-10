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

    /**
     * 다중 파일을 AWS S3에 업로드하고 업로드된 파일 경로를 반환하는 메서드
     * @param files - 업로드할 파일 목록
     * @param folder - S3에 저장될 폴더 경로
     * @return 업로드된 파일의 S3 경로 목록
     */
    public List<String> uploadMultipleFilesAndReturnPaths(List<MultipartFile> files, String folder) {
        List<String> fileUrls = new ArrayList<>(); // 업로드된 파일 경로를 저장할 리스트

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) {
                    System.out.println("빈 파일이므로 업로드 건너뜁니다. 파일 이름: " + file.getOriginalFilename());
                    continue; // 파일이 비어있으면 업로드 건너뜀
                }

                String originalFileName = file.getOriginalFilename(); // 원본 파일 이름
                String fileName = getUniqueFileName(originalFileName); // 중복되지 않는 파일 이름 생성

                byte[] fileData = file.getBytes(); // 파일 데이터를 바이트 배열로 변환

                // 파일 크기 체크. 크기가 너무 크면 예외 발생
                if (!commonMethod.fileSizeCheck(file)) {
                    throw new IllegalArgumentException("파일의 크기가 너무 큽니다: " + fileName);
                }

                // S3에 파일 업로드 요청 객체 생성
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(folder + "/" + fileName)
                        .contentType(file.getContentType()) // 파일의 MIME 타입 설정
                        .build();

                // S3에 파일 업로드
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileData));

                // 업로드된 파일의 S3 URL 경로 생성
                String s3FilePath = s3Client.utilities()
                        .getUrl(a -> a.bucket(bucket).key(folder + "/" + fileName))
                        .toExternalForm();

                fileUrls.add(s3FilePath); // 업로드된 파일의 경로를 리스트에 추가

            } catch (IOException e) {
                throw new RuntimeException("파일 업로드 중 오류 발생: " + (file != null ? file.getOriginalFilename() : "null"), e);
            } catch (Exception e) {
                throw new RuntimeException("파일 업로드 중 알 수 없는 오류 발생: " + (file != null ? file.getOriginalFilename() : "null"), e);
            }
        }

        return fileUrls; // 업로드된 파일의 경로 리스트 반환
    }

    /**
     * 파일 이름이 S3에 이미 존재하는지 확인하고, 중복되는 경우 숫자를 추가하여 고유한 파일 이름 생성
     * @param originalFileName - 원본 파일 이름
     * @return 중복되지 않는 고유한 파일 이름
     */
    private String getUniqueFileName(String originalFileName) {
        String baseName = getBaseName(originalFileName); // 확장자를 제외한 파일 이름 추출
        String extension = getExtension(originalFileName); // 파일 확장자 추출

        String fileName = baseName + extension;
        int count = 1;

        // 파일이 S3에 존재하는지 확인하고, 중복되면 숫자를 추가
        while (doesFileExistInS3(fileName)) {
            fileName = baseName + " (" + count + ")" + extension; // 중복되면 숫자 붙여서 파일 이름 생성
            count++;
        }

        return fileName; // 고유한 파일 이름 반환
    }

    /**
     * S3에 파일이 존재하는지 확인하는 메서드
     * @param fileName - 확인할 파일 이름
     * @return 파일이 존재하면 true, 존재하지 않으면 false 반환
     */
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

    /**
     * 파일 확장자를 추출하는 메서드
     * @param originalFileName - 원본 파일 이름
     * @return 파일 확장자
     */
    private String getExtension(String originalFileName) {
        if (originalFileName != null && originalFileName.contains(".")) {
            return originalFileName.substring(originalFileName.lastIndexOf(".")); // 확장자 추출
        } else {
            return ""; // 확장자가 없는 경우 빈 문자열 반환
        }
    }

    /**
     * 파일 이름에서 확장자를 제외한 이름 추출
     * @param originalFileName - 원본 파일 이름
     * @return 확장자를 제외한 파일 이름
     */
    private String getBaseName(String originalFileName) {
        if (originalFileName != null && originalFileName.contains(".")) {
            return originalFileName.substring(0, originalFileName.lastIndexOf(".")); // 확장자 제외한 파일 이름 추출
        } else {
            return originalFileName; // 확장자가 없는 경우 원래 이름 반환
        }
    }

    public String uploadFileAndReturnPath(MultipartFile file, String folder) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("빈 파일입니다.");
            }

            String originalFileName = file.getOriginalFilename();
            String fileName = getUniqueFileName(originalFileName);

            byte[] fileData = file.getBytes();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(folder + "/" + fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileData));

            return s3Client.utilities()
                    .getUrl(a -> a.bucket(bucket).key(folder + "/" + fileName))
                    .toExternalForm();

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류 발생: " + file.getOriginalFilename(), e);
        }
    }

}
