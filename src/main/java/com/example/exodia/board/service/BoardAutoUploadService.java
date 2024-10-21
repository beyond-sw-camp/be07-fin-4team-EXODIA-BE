package com.example.exodia.board.service;

import com.example.exodia.board.dto.BoardSaveReqDto;
import com.example.exodia.board.domain.Category;
import com.example.exodia.board.repository.BoardRepository;
import com.example.exodia.submit.domain.Submit;
import com.example.exodia.submit.domain.SubmitStatus;
import com.example.exodia.submit.repository.SubmitRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class BoardAutoUploadService {

    private final SubmitRepository submitRepository;
    private final BoardService boardService;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    // 임의의 장소 데이터
    private final List<String> locations = List.of("서울 강남구", "부산 해운대", "대전 중구", "인천 연수구", "대구 동구");

    // 부고 상 정보
    private final List<String> parentsDeathOptions = List.of("부친상", "모친상");
    private final List<String> grandparentsDeathOptions = List.of("조부상", "조모상", "외조부상", "외조모상");

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

            String randomWeekendDate = getRandomWeekendDate();
            String randomLocation = getRandomElement(locations);

            // 제목 생성
            String title = generateTitle(submit.getContents(), user, randomWeekendDate);
            System.out.println("Final Generated Title: " + title); // 제목 확인

            // 내용 생성
            String content = "장소: " + randomLocation + "\n일시: " + randomWeekendDate;

            // 제목과 내용을 BoardSaveReqDto에 설정
            BoardSaveReqDto boardDto = BoardSaveReqDto.builder()
                    .userNum(submit.getUserNum())
                    .category(Category.FAMILY_EVENT)
                    .title(title)  // 제목 설정
                    .content(content)  // 내용 설정
                    .build();

            System.out.println("Final Board Title: " + boardDto.getTitle()); // 최종 저장 전 제목 출력 확인
            System.out.println("Final Board Content: " + boardDto.getContent()); // 최종 저장 전 내용 출력 확인

            boardService.createBoard(boardDto, Collections.emptyList(), null);
        }
    }


    private String generateTitle(String contents, User user, String date) {
        ObjectMapper objectMapper = new ObjectMapper();
        String title = "";
        try {
            System.out.println("Raw Contents: " + contents); // 디버깅용 출력

            Map<String, String> parsedContents = objectMapper.readValue(contents, Map.class);
            System.out.println("Parsed Contents: " + parsedContents); // 파싱된 내용 출력

            String eventTypeAndRelation = parsedContents.get("경조종류");
            if (eventTypeAndRelation == null) {
                System.out.println("경조종류 값이 null입니다."); // 파싱 실패 확인
                return title;
            }

            String[] parts = eventTypeAndRelation.split(" ");
            String eventType = parts[0];
            String familyRelation = parts[1];

            String positionName = user.getPosition().getName();

            if (eventType.equals("결혼")) {
                title = "[결혼] " + user.getName() + "(" + user.getDepartment().getName() + ") " + positionName + " " + familyRelation + " 결혼 - " + date;
            } else if (eventType.equals("부고")) {
                String deathType = getDeathType(familyRelation);
                title = "[부고] " + user.getName() + "(" + user.getDepartment().getName() + ") " + positionName + " " + deathType + " - " + date;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Generated Title: " + title);

        return title;
    }



    private String getDeathType(String familyRelation) {
        switch (familyRelation) {
            case "부모":
                return getRandomElement(parentsDeathOptions);
            case "조부모":
                return getRandomElement(grandparentsDeathOptions);
            case "형제자매":
                return "형제상";
            default:
                return familyRelation;  // fallback
        }
    }

    private String getRandomElement(List<String> list) {
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

    private String getRandomWeekendDate() {
        Random random = new Random();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plus(3, ChronoUnit.MONTHS);

        List<LocalDate> weekends = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            if (startDate.getDayOfWeek().getValue() == 6 || startDate.getDayOfWeek().getValue() == 7) {
                weekends.add(startDate);
            }
            startDate = startDate.plusDays(1);
        }

        return weekends.get(random.nextInt(weekends.size())).toString();
    }
}
