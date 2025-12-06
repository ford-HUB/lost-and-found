package com.backend.server.models;

import jakarta.persistence.*;
@Entity
@Table(name = "roles")

public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long role_id;

    @Column(
        nullable = false,
        length = 55
    )
    private String role_name;

    @Column(
        nullable = true,
        length = 255
    )
    private String description;

    public Role () {} // Initialize Contructor

    public Role (Long role_id, String role_name, String description) {
        this.role_id = role_id;
        this.role_name = role_name;
        this.description = description;
    }

    // @getters
    public Long getRoleId() { return role_id; }
    public String getRoleName() { return role_name; }
    public String getDescription() { return description; }

    // @setters
    public void setRoleId(Long role_id) { this.role_id = role_id; }
    public void setRoleName(String role_name) { this.role_name = role_name; }
    public void setDescription(String description) { this.description = description; }

}
