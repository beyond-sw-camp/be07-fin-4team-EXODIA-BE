package com.example.exodia.chat.repository;

import com.example.exodia.chat.domain.ChatMessage;
import com.example.exodia.common.domain.DelYN;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByChatRoomId(Long roomId);
}
