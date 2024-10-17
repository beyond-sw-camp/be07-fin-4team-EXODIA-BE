package com.example.exodia.board.service;

import com.example.exodia.board.domain.BoardTags;
import com.example.exodia.board.dto.TagDto;
import com.example.exodia.board.repository.BoardTagRepository;
import com.example.exodia.board.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final BoardTagRepository boardTagRepository;

    @Autowired
    public TagService(TagRepository tagRepository, BoardTagRepository boardTagRepository) {
        this.tagRepository = tagRepository;
        this.boardTagRepository = boardTagRepository;
    }

    /**
     * 태그 생성 메서드
     * @param tagDto - 생성할 태그 정보
     * @return 생성된 태그 엔티티
     */
    @Transactional
    public BoardTags createTag(TagDto tagDto) {
        BoardTags newTag = BoardTags.builder()
                .tag(tagDto.getTag())
                .build();
        return tagRepository.save(newTag);
    }

    /**
     * 모든 태그 조회 메서드
     * @return 태그 리스트
     */
    public List<TagDto> getAllTags() {
        List<BoardTags> boardTagsList = tagRepository.findAll();
        return boardTagsList.stream()
                .map(tag -> new TagDto(tag.getId(), tag.getTag()))
                .collect(Collectors.toList());
    }

    /**
     * 태그 삭제 메서드
     * @param id - 삭제할 태그의 ID
     */
    @Transactional
    public void deleteTag(Long id) {
        // board_tag 테이블에서 해당 태그와 연결된 모든 레코드를 먼저 삭제
        boardTagRepository.deleteByTagId(id);

        // 태그 삭제
        BoardTags tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("태그를 찾을 수 없습니다."));

        tagRepository.delete(tag);
    }

}
