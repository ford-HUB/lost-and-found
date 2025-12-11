package com.backend.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.server.models.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // @check the email if exist
    boolean existsByEmail(String email);

    // @auth get email if found
    Optional<Account> findByEmail(String email);
}
