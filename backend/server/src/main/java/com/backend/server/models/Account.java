package com.backend.server.models;

import jakarta.persistence.*;

@Entity
@Table(name="accounts")

public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long account_id;

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

    public Account() {} // initialize the contructor

    public Account(Long accountId, String email, String password) {
        this.account_id = accountId;
        this.email = email;
        this.password = password;
    }

    // @getters
    public Long getAccountId() { return account_id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    // @setters
    public void setAccountId(Long account_id) { this.account_id = account_id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}
