package com.backend.server.models;

import jakarta.persistence.*;
@Entity
@Table(name = "roles")

public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long role_id;

    @OneToOne
    @JoinColumn(
        name = "accounts",
        referencedColumnName = "account_id",
        nullable = false
    )
    private Account account;

    @Column(
        nullable = false,
        length = 55
    )
    private String roleName;

    @Column(
        nullable = true,
        length = 255
    )
    private String description;

    public Role () {} // Initialize Contructor

    public Role (Long role_id, String roleName, String description, Account account) {
        this.role_id = role_id;
        this.roleName = roleName;
        this.description = description;
        this.account = account;
    }

    // @getters
    public Long getRoleId() { return role_id; }
    public String getRoleName() { return roleName; }
    public String getDescription() { return description; }
    public Account getAccount() { return account; }

    // @setters
    public void setRoleId(Long role_id) { this.role_id = role_id; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public void setDescription(String description) { this.description = description; }
    public void setAccount(Account account) { this.account = account; }

}
