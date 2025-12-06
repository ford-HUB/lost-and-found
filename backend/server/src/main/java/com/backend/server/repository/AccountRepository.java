package com.backend.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.server.models.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // @check the email if exist
    boolean existsByEmail(String email);
}
