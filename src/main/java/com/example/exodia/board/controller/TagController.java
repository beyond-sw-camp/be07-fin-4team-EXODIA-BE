package com.example.exodia.board.controller;

import com.example.exodia.board.domain.BoardTags;
import com.example.exodia.board.dto.TagDto;
import com.example.exodia.board.service.TagService;
import com.example.exodia.common.dto.CommonErrorDto;
import com.example.exodia.common.dto.CommonResDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTag(@RequestBody TagDto tagDto) {
        try {
            BoardTags newTag = tagService.createTag(tagDto);
            CommonResDto response = new CommonResDto(HttpStatus.CREATED, "태그가 성공적으로 추가되었습니다.", newTag);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getTagList() {
        try {
            List<TagDto> tags = tagService.getAllTags();
            CommonResDto response = new CommonResDto(HttpStatus.OK, "태그 목록을 반환합니다.", tags);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable Long id) {
        try {
            tagService.deleteTag(id);
            CommonResDto response = new CommonResDto(HttpStatus.OK, "태그가 성공적으로 삭제되었습니다.", id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            CommonErrorDto errorResponse = new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
