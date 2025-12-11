package com.backend.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.server.models.UserSettings;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    
    // @find settings by account
    Optional<UserSettings> findByAccount_AccountId(Long accountId);
}
