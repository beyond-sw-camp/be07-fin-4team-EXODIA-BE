package com.example.exodia.user.service;

import com.example.exodia.common.auth.JwtTokenProvider;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.dto.UserLoginDto;
import com.example.exodia.user.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String login(UserLoginDto loginDto) {
        User user = userRepository.findById(loginDto.getId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        if (user.isDeleted()) {
            throw new RuntimeException("비활성화 상태의 계정입니다.");
        }

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            user.incrementLoginFailCount();

            if (user.getLoginFailCount() >= 5) {
                user.softDelete();
            }

            userRepository.save(user);
            throw new RuntimeException("잘못된 이메일/비밀번호 입니다.");
        }

        user.resetLoginFailCount();
        userRepository.save(user);

        return jwtTokenProvider.createToken(user.getId(),
                user.getDepartment().getId(),
                user.getPosition().getId());
    }
}
