package com.example.exodia.chat.repository;

import com.example.exodia.chat.domain.ChatUser;
import com.example.exodia.common.domain.DelYN;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
//    List<ChatUser> findAllByUserNumAndDelYn(String userNum, DelYN delYn);
}
