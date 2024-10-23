//package com.example.exodia.common.service;
//
//import com.example.exodia.common.auth.JwtTokenProvider;
//import com.example.exodia.user.domain.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.concurrent.TimeUnit;
//
//@Service
//public class AuthService {
//
//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;
//
//    @Autowired
//    private JwtTokenProvider jwtTokenProvider;
//
//    public String login(User user) {
//        String token = jwtTokenProvider.createToken(user.getUserNum(), user.getDepartment().getId(), user.getPosition().getId());
//
//        String existingToken = redisTemplate.opsForValue().get(user.getUserNum());
//
//        if (existingToken != null) {
//            redisTemplate.delete(user.getUserNum());
//        }
//
//        redisTemplate.opsForValue().set(user.getUserNum(), token, jwtTokenProvider.getTokenExpiration(), TimeUnit.MILLISECONDS);
//
//        return token;
//    }
//
//    public boolean isTokenValid(String token) {
//        String userNum = jwtTokenProvider.getUserNumFromToken(token);
//        String storedToken = redisTemplate.opsForValue().get(userNum);
//
//        return token.equals(storedToken);
//    }
//}
//
