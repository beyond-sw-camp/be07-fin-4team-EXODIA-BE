package com.example.exodia.board.service;

import com.example.exodia.board.domain.Tags;

import com.example.exodia.board.dto.TagDto;
import com.example.exodia.board.repository.BoardTagsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagService {

    private final BoardTagsRepository boardTagsRepository;

    @Autowired
    public TagService(BoardTagsRepository boardTagsRepository) {
        this.boardTagsRepository = boardTagsRepository;
    }

    @Transactional
    public Tags createTag(TagDto tagDto) {
        Tags newTag = Tags.builder()
                .tag(tagDto.getTag())
                .build();
        return boardTagsRepository.save(newTag);
    }

    @Transactional
    public void deleteTag(Long id) {
        Tags tag = boardTagsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("태그를 찾을 수 없습니다."));
        boardTagsRepository.delete(tag);
    }
}
