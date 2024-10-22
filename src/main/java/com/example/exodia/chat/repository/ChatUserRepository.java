package com.example.exodia.chat.repository;

import com.example.exodia.chat.domain.ChatRoom;
import com.example.exodia.chat.domain.ChatUser;
import com.example.exodia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
    List<ChatUser> findAllByUser(User user);
    List<ChatUser> findAllByChatRoom(ChatRoom chatRoom);
    Optional<ChatUser> findByUserAndChatRoom(User user, ChatRoom chatroom);
}
