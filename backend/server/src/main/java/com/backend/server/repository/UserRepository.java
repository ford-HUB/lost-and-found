package com.backend.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.server.models.Account;
import com.backend.server.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAccount(Account account);
}
