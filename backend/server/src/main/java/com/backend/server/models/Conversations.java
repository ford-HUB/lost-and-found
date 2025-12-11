package com.backend.server.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "conversations")

public class Conversations {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Long conversationId;

    @ManyToOne
    @JoinColumn(
        name = "items",
        referencedColumnName = "item_id",
        nullable = false
    )
    private Items item;

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "sender_id",
        referencedColumnName = "account_id",
        nullable = false
    )
    private Account sender;

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "receiver_id",
        referencedColumnName = "account_id",
        nullable = false
    )
    private Account receiver;

    @Column(
        name = "created_at",
        nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
        name = "last_message_at",
        nullable = true
    )
    private LocalDateTime lastMessageAt;

    public Conversations() {} // initialize constructor

    public Conversations(Long conversationId, Items item, Account sender, Account receiver, 
                         LocalDateTime createdAt, LocalDateTime lastMessageAt) {
        this.conversationId = conversationId;
        this.item = item;
        this.sender = sender;
        this.receiver = receiver;
        this.createdAt = createdAt;
        this.lastMessageAt = lastMessageAt;
    }

    // @getters
    public Long getConversationId() { return conversationId; }
    public Items getItem() { return item; }
    public Account getSender() { return sender; }
    public Account getReceiver() { return receiver; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }

    // @setters
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public void setItem(Items item) { this.item = item; }
    public void setSender(Account sender) { this.sender = sender; }
    public void setReceiver(Account receiver) { this.receiver = receiver; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

}

