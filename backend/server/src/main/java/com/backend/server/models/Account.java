package com.backend.server.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name="accounts")

public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(
        unique = true,
        nullable = false
    )
    private String email;

    @Column(
        length = 255,
        nullable = false
    )
    private String password;

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Account() {} // initialize the contructor

    public Account(Long accountId, String email, String password) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
    }

    // @getters
    public Long getAccountId() { return accountId; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // @setters
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
