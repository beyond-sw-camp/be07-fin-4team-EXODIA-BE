package com.example.exodia.board.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class BoardHitsService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 조회수 관리용 RedisTemplate 주입
    public BoardHitsService(@Qualifier("hits") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /* 유저의 게시물 조회 여부 확인 및 조회수 증가 */
    public Long incrementBoardHits(Long boardId, String user_num) {
        String key = "board_hits:" + boardId; // 조회수 관리 키
        String userKey = "board_hits_user:" + boardId; // 사용자 조회 기록 관리 키

        // 해당 유저가 이미 이 게시물을 조회했는지 확인
        Boolean hasViewed = redisTemplate.opsForSet().isMember(userKey, user_num);

        if (Boolean.FALSE.equals(hasViewed)) {
            // 처음 조회하는 사용자라면 조회수 증가
            System.out.println("사용자 [" + user_num + "]가 게시물 [" + boardId + "]을 처음 조회합니다.");

            // 사용자 조회 기록 추가
            redisTemplate.opsForSet().add(userKey, user_num);

            // 조회수 증가
            Long newHits = redisTemplate.opsForValue().increment(key);
            System.out.println("게시물 [" + boardId + "]의 조회수가 증가되었습니다. 새로운 조회수: " + newHits);

            return newHits;
        }

        // 이미 조회한 경우 조회수 증가하지 않음
        System.out.println("사용자 [" + user_num + "]가 이미 게시물 [" + boardId + "]을 조회한 적 있습니다.");
        return getBoardHits(boardId); // 기존 조회수 반환
    }

    // 조회수 가져오기
    public Long getBoardHits(Long boardId) {
        String key = "board_hits:" + boardId; // 조회수 관리 키
        Object hits = redisTemplate.opsForValue().get(key); // Redis에서 조회수 조회

        if (hits instanceof Integer) {
            return ((Integer) hits).longValue();
        } else if (hits instanceof Long) {
            return (Long) hits;
        }

        // 조회수가 없을 경우 0 반환
        System.out.println("게시물 [" + boardId + "]의 조회수가 없습니다. 기본값 0을 반환합니다.");
        return 0L;
    }

    /* 게시물 생성 시 기존 조회수 및 사용자 조회 기록 초기화 */
    public void resetBoardHits(Long boardId) {
        String key = "board_hits:" + boardId;
        String userKey = "board_hits_user:" + boardId;

        // Redis에서 조회수와 사용자 기록 삭제
        redisTemplate.delete(key);
        redisTemplate.delete(userKey);

        System.out.println("게시물 [" + boardId + "]의 조회수 및 사용자 조회 기록이 초기화되었습니다.");
    }
}
