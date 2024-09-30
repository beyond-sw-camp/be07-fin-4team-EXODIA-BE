package com.example.exodia.board.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service // 해당 클래스가 서비스 레이어의 역할을 수행하며, 스프링 빈으로 등록됨을 나타냄
public class BoardHitsService {

    private final RedisTemplate<String, Object> redisTemplate;

    // RedisTemplate을 주입받아 초기화
    public BoardHitsService(@Qualifier("hits") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 특정 게시물의 조회수를 증가시키는 메서드
     * @param boardId - 조회수를 증가시킬 게시물의 고유 ID
     * @param userNum - 조회수를 증가시키는 사용자의 ID (사번 or 사용자 식별자)
     * @return 증가된 조회수(Long 타입) 반환
     */
    public Long incrementBoardHits(Long boardId, String userNum) {
        // 조회수를 관리하기 위한 Redis 키 설정
        String key = "board_hits:" + boardId;

        // 해당 게시물을 조회한 사용자 목록을 관리하기 위한 키 설정
        String userKey = "board_hits_user:" + boardId;

        // key: board_hits:1 → 게시물 ID가 1인 게시물의 조회수를 저장하는 키
        // userKey: board_hits_user:1 → 게시물 ID가 1인 게시물을 조회한 사용자 목록을 저장하는 키

        // 사용자가 이미 해당 게시물을 조회했는지 확인
        Boolean hasViewed = redisTemplate.opsForSet().isMember(userKey, userNum);

        // 처음 조회하는 경우 조회수 증가
        if (Boolean.FALSE.equals(hasViewed)) {
            // 사용자 조회 기록에 현재 사용자 추가
            redisTemplate.opsForSet().add(userKey, userNum);

            // 게시물 조회수를 1만큼 증가
            Long newHits = redisTemplate.opsForValue().increment(key);

            // 증가된 조회수 반환
            return newHits;
        }

        // 이미 조회한 경우, 조회수를 증가시키지 않고 기존 조회수를 반환
        System.out.println("사용자 [" + userNum + "]가 이미 게시물 [" + boardId + "]을 조회한 적 있습니다.");
        return getBoardHits(boardId); // 기존 조회수 반환
    }

    /**
     * 특정 게시물의 현재 조회수를 Redis에서 가져오는 메서드
     * @param boardId - 조회수를 가져올 게시물의 고유 ID
     * @return 조회수(Long 타입) 반환. 조회수가 없는 경우 0 반환
     */
    public Long getBoardHits(Long boardId) {
        // 조회수를 관리하기 위한 Redis 키 설정
        String key = "board_hits:" + boardId;

        // Redis에서 조회수를 조회
        Object hits = redisTemplate.opsForValue().get(key);

        // 조회수가 Integer 타입일 경우 Long 타입으로 변환하여 반환
        if (hits instanceof Integer) {
            return ((Integer) hits).longValue();
        }
        // 조회수가 Long 타입일 경우 그대로 반환
        else if (hits instanceof Long) {
            return (Long) hits;
        }

        // 조회수가 없는 경우 기본값 0 반환
        return 0L;
    }

    /**
     * 게시물이 새로 생성될 때, 기존의 조회수 및 사용자 조회 기록을 초기화하는 메서드
     * @param boardId - 초기화할 게시물의 고유 ID
     */
    public void resetBoardHits(Long boardId) {
        // 조회수 관리용 Redis 키
        String key = "board_hits:" + boardId;

        // 사용자 조회 기록 관리용 Redis 키
        String userKey = "board_hits_user:" + boardId;

        // 기존 조회수와 사용자 조회 기록을 Redis에서 삭제하여 초기화
        redisTemplate.delete(key);
        redisTemplate.delete(userKey);
    }
}
