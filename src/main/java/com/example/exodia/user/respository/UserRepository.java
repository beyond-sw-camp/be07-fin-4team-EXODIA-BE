package com.example.exodia.user.respository;

import com.example.exodia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface UserRepository extends JpaRepository<User, String> {
    @Query("select u from u where u.delYn = 'Y'")
    List<User> findDeletedUsers();

}
