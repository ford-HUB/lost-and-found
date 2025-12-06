package com.backend.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.server.models.User;

public interface UserRepository extends JpaRepository<User, Long> { }
