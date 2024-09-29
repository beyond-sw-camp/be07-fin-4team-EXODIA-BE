package com.example.exodia.chat.repository;

import com.example.exodia.chat.domain.ChatMessage;
import com.example.exodia.common.domain.DelYN;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByRoomIdAndDelYn(Long roomId, DelYN delYN);
}
