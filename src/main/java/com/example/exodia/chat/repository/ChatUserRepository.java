package com.example.exodia.chat.repository;

import com.example.exodia.chat.domain.ChatUser;
import com.example.exodia.common.domain.DelYN;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
    List<ChatUser> findAllByUserNumAndDelYn(String userNum, DelYN delYn);
}
