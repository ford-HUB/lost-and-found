package com.backend.server.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "likes")

public class Likes {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long likeId;

    @ManyToOne
    @JoinColumn(
        name = "accounts",
        referencedColumnName = "account_id",
        nullable = false
    )
    private Account account;

    @ManyToOne
    @JoinColumn(
        name = "items",
        referencedColumnName = "item_id",
        nullable = false
    )
    private Items item;

    @Column(
        name = "created_at",
        nullable = false
    )
    private LocalDateTime createdAt;

    public Likes() {} // initialize constructor

    public Likes(Long likeId, Account account, Items item, LocalDateTime createdAt) {
        this.likeId = likeId;
        this.account = account;
        this.item = item;
        this.createdAt = createdAt;
    }

    // @getters
    public Long getLikeId() { return likeId; }
    public Account getAccount() { return account; }
    public Items getItem() { return item; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // @setters
    public void setLikeId(Long likeId) { this.likeId = likeId; }
    public void setAccount(Account account) { this.account = account; }
    public void setItem(Items item) { this.item = item; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}

