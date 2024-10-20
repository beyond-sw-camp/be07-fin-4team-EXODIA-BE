package com.example.exodia.board.service;

import com.example.exodia.board.dto.BoardSaveReqDto;
import com.example.exodia.board.domain.Category;
import com.example.exodia.board.repository.BoardRepository;
import com.example.exodia.submit.domain.Submit;
import com.example.exodia.submit.domain.SubmitStatus;
import com.example.exodia.submit.repository.SubmitRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class BoardAutoUploadService {

    private final SubmitRepository submitRepository;
    private final BoardService boardService;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    // 임의의 장소 및 일시 데이터
    private final List<String> locations = List.of("서울 강남구", "부산 해운대", "대전 중구", "인천 연수구", "대구 동구");
    private final List<String> dates = List.of("2024-05-01", "2024-06-15", "2024-07-10", "2024-08-20", "2024-09-01");

    public BoardAutoUploadService(SubmitRepository submitRepository, BoardService boardService, UserRepository userRepository, BoardRepository boardRepository) {
        this.submitRepository = submitRepository;
        this.boardService = boardService;
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
    }

    @Transactional
    public void checkAndUploadFamilyEvent(Long submitId) {
        Submit submit = submitRepository.findById(submitId)
                .orElseThrow(() -> new EntityNotFoundException("해당 결재 문서를 찾을 수 없습니다."));

        if (submit.getSubmitStatus() == SubmitStatus.ACCEPT && submit.isUploadBoard()) {
            User user = userRepository.findByUserNum(submit.getUserNum())
                    .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));

            String randomDate = getRandomElement(dates);
            String randomLocation = getRandomElement(locations);

            BoardSaveReqDto boardDto = new BoardSaveReqDto();
            boardDto.setUserNum(submit.getUserNum());
            boardDto.setCategory(Category.FAMILY_EVENT);
            boardDto.setUploadBoard(true);

            if (submit.getSubmitType().equals("본인의 결혼")) {
                boardDto.setTitle("🎉 " + user.getDepartment().getName() + " " + user.getPosition() + " " + user.getName() + "님의 결혼식 🎉");
                boardDto.setContent("일시: " + randomDate + "\n장소: " + randomLocation);
            } else if (submit.getSubmitType().contains("사망")) {
                boardDto.setTitle("🔔 " + user.getDepartment().getName() + " " + user.getPosition() + " " + user.getName() + "님의 부고 🔔");
                boardDto.setContent("발인일: " + randomDate + "\n장례식 장소: " + randomLocation);
            }

            boardService.createBoard(boardDto, Collections.emptyList(), null);
        }
    }

    private String getRandomElement(List<String> list) {
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }
}
