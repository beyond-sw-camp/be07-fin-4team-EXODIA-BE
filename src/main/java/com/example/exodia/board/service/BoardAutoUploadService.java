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
    private final List<String> locations = List.of("서울 강남구", "부산 해운대", "대전 중구", "인천 연수구",
            "대구 동구", "서울 관악구", "익산 마동", "서울 마포구", "서울 종구", "서울 종로구", "서울 도봉구", "서울 노원구", "서울 강동구",
            "서울 성동구", "서울 광진구", "서울 송파구", "서울 서초구", "서울 금천구", "익산 영등2동", "익산 영등1동", "익산 어양동", "익산 남중동",
            "익산 인화동", "익산 중앙동", "익산 송학동", "대전광역시 대덕구", "대전광역시 유성구", "대전광역시 동구", "광주광역시 남구 효덕로");

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

        if (submit.getSubmitStatus() == SubmitStatus.승인 && submit.isUploadBoard()) {
            User user = userRepository.findByUserNum(submit.getUserNum())
                    .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));

            String randomLocation = getRandomElement(locations);

            // 경조종류를 정확하게 추출 (부고 또는 결혼 여부 확인)
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> parsedContents;
            String eventType;
            try {
                parsedContents = objectMapper.readValue(submit.getContents(), Map.class);
                eventType = parsedContents.get("경조종류").split(" ")[0];  // 경조사 종류 추출 (예: 부고, 결혼)
            } catch (Exception e) {
                e.printStackTrace();
                return;  // 파싱 실패 시 중단
            }

            // 결혼, 부고에 따라 다른 날짜 생성
            String randomDate = eventType.equals("결혼") ? getRandomDate(4, ChronoUnit.MONTHS) : getRandomDate(2, ChronoUnit.WEEKS);

            String title = generateTitle(submit.getContents(), user, randomDate);
            System.out.println("Final Generated Title: " + title); // 제목 확인

            String content = generateContent(submit.getContents(), user, randomLocation, randomDate, eventType);

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

            // 상 종류를 한번만 선택해 제목과 내용에 동일하게 사용
            String deathType = getDeathType(familyRelation);

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
        // 상 종류는 제목과 내용에서 동일하게 사용하기 위해 한번만 추출
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

    private String getRandomDate(int amount, ChronoUnit unit) {
        Random random = new Random();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plus(amount, unit);

        List<LocalDate> possibleDates = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            possibleDates.add(startDate);
            startDate = startDate.plusDays(1);
        }

        return possibleDates.get(random.nextInt(possibleDates.size())).toString();
    }

    private String generateContent(String contents, User user, String location, String date, String eventType) {
        ObjectMapper objectMapper = new ObjectMapper();
        String content = "";

        try {
            System.out.println("Raw contents: " + contents);  // 디버깅용 원본 로그
            System.out.println("Event Type: " + eventType);  // 이벤트 타입 확인

            // contents JSON 파싱
            Map<String, String> parsedContents = objectMapper.readValue(contents, Map.class);
            System.out.println("Parsed Contents: " + parsedContents);  // 파싱된 데이터 로그

            String eventTypeAndRelation = parsedContents.get("경조종류");
            if (eventTypeAndRelation == null) {
                System.out.println("경조종류 값이 null입니다.");
                return content; // 여기서 중단하고 반환
            }

            String familyRelation = eventTypeAndRelation.split(" ")[1];  // 예: "부고 부모"에서 "부모" 추출
            System.out.println("Family Relation: " + familyRelation);  // 가족 관계 로그

            // 상 종류를 한번만 추출하여 제목과 내용에 동일하게 사용
            String deathType = getDeathType(familyRelation);
            System.out.println("Death Type: " + deathType);  // 상 종류 확인

            if (eventType.equals("부고")) {
                content = String.format(
                        "<div>[부고]</div>" +
                                "<div>%s 부서 %s(%s)님의 %s께서 별세하셨습니다.</div>" +
                                "<div> 삼가 고인의 명복을 빕니다.</div>" +
                                "<div>빈소: %s</div>" +
                                "<div>발인일: %s</div>" +
                                "<div>많은 위로 부탁드립니다.</div>",
                        user.getDepartment().getName(),
                        user.getName(),
                        user.getPosition().getName(),
                        deathType,
                        location,
                        date
                );
            } else if (eventType.equals("결혼")) {
                content = String.format(
                        "<div>[결혼]</div>" +
                                "<div>%s 부서 %s(%s)님의 결혼 소식을 알려드립니다.</div>" +
                                "<div>일시: %s</div>" +
                                "<div>장소: %s 예식장</div>" +
                                "<div>많은 축하 부탁드립니다.</div>",
                        user.getDepartment().getName(),
                        user.getName(),
                        user.getPosition().getName(),
                        date,
                        location
                );
            }

            System.out.println("Generated Content: " + content);  // 생성된 content 확인

        } catch (Exception e) {
            e.printStackTrace();
        }

        return content;
    }
}
