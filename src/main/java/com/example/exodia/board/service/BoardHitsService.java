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
        String key = "board_hits:" + boardId;
        String userKey = "board_hits_user:" + boardId;

        // 해당 유저가 이미 이 게시물을 조회했는지 확인
        Boolean hasViewed = redisTemplate.opsForSet().isMember(userKey, user_num);

        if (Boolean.FALSE.equals(hasViewed)) {
            // 조회한 적이 없으면 조회수 증가 및 사용자 기록 추가
            redisTemplate.opsForSet().add(userKey, user_num);
            return redisTemplate.opsForValue().increment(key);
        }

        // 이미 조회한 경우 조회수 증가하지 않음
        return getBoardHits(boardId);
    }

    // 조회수 가져오기
    public Long getBoardHits(Long boardId) {
        String key = "board_hits:" + boardId;
        Object hits = redisTemplate.opsForValue().get(key);
        if (hits instanceof Integer) {
            return ((Integer) hits).longValue();
        } else if (hits instanceof Long) {
            return (Long) hits;
        }

        return 0L; // 조회수가 없을 경우 0 반환
    }
}

