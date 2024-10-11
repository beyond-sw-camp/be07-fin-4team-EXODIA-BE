package com.example.exodia.chat.repository;

import com.example.exodia.chat.domain.ChatRoom;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
