package com.example.exodia.chat.repository;

import com.example.exodia.chat.domain.ChatUser;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
    List<ChatUser> findAllByUserAndDelYn(User user, DelYN delYn);
}
