package com.example.exodia.board.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class BoardHitsService {

    private final RedisTemplate<String, Object> redisTemplate;


    public BoardHitsService(@Qualifier("hits") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public Long incrementBoardHits(Long boardId, String userNum) {
        String key = "board_hits:" + boardId;
        String userKey = "board_hits_user:" + boardId;
        Boolean hasViewed = redisTemplate.opsForSet().isMember(userKey, userNum);

        if (Boolean.FALSE.equals(hasViewed)) {
            redisTemplate.opsForSet().add(userKey, userNum);
            Long newHits = redisTemplate.opsForValue().increment(key);
            return newHits;
        }

        return getBoardHits(boardId);
    }

    public Long getBoardHits(Long boardId) {
        String key = "board_hits:" + boardId;
        Object hits = redisTemplate.opsForValue().get(key);
        if (hits instanceof Integer) {
            return ((Integer) hits).longValue();
        }

        else if (hits instanceof Long) {
            return (Long) hits;
        }

        return 0L;
    }

    public void resetBoardHits(Long boardId) {
        String key = "board_hits:" + boardId;
        String userKey = "board_hits_user:" + boardId;
        redisTemplate.delete(key);
        redisTemplate.delete(userKey);
    }
}
