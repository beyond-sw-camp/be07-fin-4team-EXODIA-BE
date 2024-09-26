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

    /* 조회수 증가 */
    public Long incrementBoardHits(Long boardId) {
        String key = "board_hits:" + boardId;
        return redisTemplate.opsForValue().increment(key);
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

        return 0L; // 값이 없으면 기본값 0L 반환


    }
}
