package com.example.exodia.board.service;

import com.example.exodia.board.domain.BoardTags;
import com.example.exodia.board.dto.TagDto;
import com.example.exodia.board.repository.BoardTagRepository;
import com.example.exodia.board.repository.BoardTagsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final BoardTagsRepository boardTagsRepository;
    private final BoardTagRepository boardTagRepository;

    @Autowired
    public TagService(BoardTagsRepository boardTagsRepository, BoardTagRepository boardTagRepository) {
        this.boardTagsRepository = boardTagsRepository;
        this.boardTagRepository = boardTagRepository;
    }


    @Transactional
    public BoardTags createTag(TagDto tagDto) {
        BoardTags newTag = BoardTags.builder()
                .tag(tagDto.getTag())
                .build();
        return boardTagsRepository.save(newTag);
    }


    public List<TagDto> getAllTags() {
        List<BoardTags> boardTagsList = boardTagsRepository.findAll();
        return boardTagsList.stream()
                .map(tag -> new TagDto(tag.getId(), tag.getTag()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTag(Long id) {
        boardTagRepository.deleteByTagId(id);

        BoardTags tag = boardTagsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("태그를 찾을 수 없습니다."));

        boardTagsRepository.delete(tag);
    }

}
