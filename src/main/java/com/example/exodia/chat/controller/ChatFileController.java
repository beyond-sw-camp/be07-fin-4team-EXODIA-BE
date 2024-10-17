package com.example.exodia.chat.controller;

//import com.example.exodia.chat.dto.ChatFileMetaDataRequest;
//import com.example.exodia.chat.dto.ChatFileMetaDataResponse;
import com.example.exodia.chat.dto.ChatFileRequest;
import com.example.exodia.chat.service.FileUploadService;
import com.example.exodia.common.dto.CommonResDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class ChatFileController {
    private final FileUploadService fileUploadService;

    @Autowired
    public ChatFileController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/presigned-urls")
    public ResponseEntity<?> generatePresignedUrls(@RequestBody List<ChatFileRequest> files) {
        Map<String, String> presignedUrls = fileUploadService.generatePresignedUrls(files);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Presigned URLs 생성 성공", presignedUrls);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

//    @PostMapping("/metadata")
//    public ResponseEntity<?> saveFileMetadata(@RequestBody ChatFileMetaDataRequest fileMetadataList) {
//        List<ChatFileMetaDataResponse> savedMetadata = fileUploadService.saveChatFileMetaData(fileMetadataList);
//        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "File metadata 저장 성공", savedMetadata);
//        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
//    }

    //    presigned url to download
    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> getPresignedUrlToDownload (@PathVariable Long fileId) {
        String presignedUrl = fileUploadService.getPresignedUrlToDownload(fileId, "chatFile");
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "다운로드 Presigned URL 조회 성공", presignedUrl);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
