package com.example.exodia.user.repository;

import com.example.exodia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserId(String userId);
}
