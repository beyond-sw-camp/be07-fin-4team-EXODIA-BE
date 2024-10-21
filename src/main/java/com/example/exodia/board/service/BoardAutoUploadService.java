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

            String title = generateTitle(submit.getContents(), user, randomWeekendDate);
            System.out.println("Final Generated Title: " + title); // 제목 확인

            String content = generateContent(submit.getContents(), user, randomLocation, randomWeekendDate, "부고");

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
            Map<String, String> parsedContents = objectMapper.readValue(contents, Map.class);

            String eventTypeAndRelation = parsedContents.get("경조종류");
            if (eventTypeAndRelation == null) {
                System.out.println("경조종류 값이 null입니다."); // 파싱 실패 확인
                return title;
            }

            String[] parts = eventTypeAndRelation.split(" ");
            String eventType = parts[0];
            String familyRelation = parts[1];

            String positionName = user.getPosition().getName(); // 올바르게 직급명 추출

            if (eventType.equals("결혼")) {
                title = String.format("[결혼] %s(%s) %s %s 결혼 - %s",
                        user.getName(),
                        user.getDepartment().getName(),
                        positionName,
                        familyRelation,
                        date
                );
            } else if (eventType.equals("부고")) {
                String deathType = getDeathType(familyRelation);
                title = String.format("[부고] %s(%s) %s %s - %s",
                        user.getName(),
                        user.getDepartment().getName(),
                        positionName,
                        deathType,
                        date
                );
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


    private String generateContent(String contents, User user, String location, String date, String eventType) {
        ObjectMapper objectMapper = new ObjectMapper();
        String content = "";

        try {
            Map<String, String> parsedContents = objectMapper.readValue(contents, Map.class);
            String eventTypeAndRelation = parsedContents.get("경조종류");

            if (eventType.equals("부고")) {
                content = String.format(
                        "[부고]\n%s 부서 %s(%s)님의 %s께서 별세하셨기에 삼가 고인의 명복을 빕니다.\n" +
                                "빈소: %s\n발인일: %s\n많은 위로 부탁드립니다.",
                        user.getDepartment().getName(),
                        user.getName(),
                        user.getPosition().getName(),
                        getDeathType(eventTypeAndRelation.split(" ")[1]),
                        location,
                        date
                );
            } else if (eventType.equals("결혼")) {
                content = String.format(
                        "[결혼]\n%s 부서 %s(%s)님의 결혼식 소식을 알려드립니다.\n" +
                                "일시: %s\n장소: %s 예식장\n많은 축하 부탁드립니다.",
                        user.getDepartment().getName(),
                        user.getName(),
                        user.getPosition().getName(),
                        date,
                        location
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return content;
    }
}
