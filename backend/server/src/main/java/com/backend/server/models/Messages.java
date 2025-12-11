package com.backend.server.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "messages")

public class Messages {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne
    @JoinColumn(
        name = "conversations",
        referencedColumnName = "conversation_id",
        nullable = false
    )
    private Conversations conversation;

    @ManyToOne
    @JoinColumn(
        name = "sender_id",
        referencedColumnName = "user_id",
        nullable = false
    )
    private User sender;

    @Column(
        name = "message_text",
        nullable = false
    )
    private String messageText;

    @Column(
        name = "is_read",
        nullable = false
    )
    private Boolean isRead;

    @Column(
        name = "created_at",
        nullable = false
    )
    private LocalDateTime createdAt;

    public Messages() {} // initialize constructor

    public Messages(Long messageId, Conversations conversation, User sender, String messageText, 
                    Boolean isRead, LocalDateTime createdAt) {
        this.messageId = messageId;
        this.conversation = conversation;
        this.sender = sender;
        this.messageText = messageText;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // @getters
    public Long getMessageId() { return messageId; }
    public Conversations getConversation() { return conversation; }
    public User getSender() { return sender; }
    public String getMessageText() { return messageText; }
    public Boolean getIsRead() { return isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // @setters
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public void setConversation(Conversations conversation) { this.conversation = conversation; }
    public void setSender(User sender) { this.sender = sender; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}

