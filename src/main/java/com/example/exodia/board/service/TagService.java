package com.example.exodia.board.service;

import com.example.exodia.board.domain.Tags;
import com.example.exodia.board.dto.TagDto;
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

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * 태그 생성 메서드
     * @param tagDto - 생성할 태그 정보
     * @return 생성된 태그 엔티티
     */
    @Transactional
    public Tags createTag(TagDto tagDto) {
        Tags newTag = Tags.builder()
                .tag(tagDto.getTag())
                .build();
        return tagRepository.save(newTag);
    }

    /**
     * 모든 태그 조회 메서드
     * @return 태그 리스트
     */
    public List<TagDto> getAllTags() {
        List<Tags> tagsList = tagRepository.findAll();
        return tagsList.stream()
                .map(tag -> new TagDto(tag.getId(), tag.getTag()))
                .collect(Collectors.toList());
    }

    /**
     * 태그 삭제 메서드
     * @param id - 삭제할 태그의 ID
     */
    @Transactional
    public void deleteTag(Long id) {
        Tags tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("태그를 찾을 수 없습니다."));
        tagRepository.delete(tag);
    }
}
