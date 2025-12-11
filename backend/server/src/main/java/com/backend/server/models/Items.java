package com.backend.server.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "items")

public class Items {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne
    @JoinColumn(
        name = "accounts",
        referencedColumnName = "account_id",
        nullable = false
    )
    private Account account;

    @Column(
        name = "item_name",
        nullable = false,
        length = 255
    )
    private String itemName;

    @Column(
        nullable = false
    )
    private String description;

    @Column(
        nullable = false,
        length = 255
    )
    private String location;

    @Column(
        name = "date_found",
        nullable = false
    )
    private LocalDate dateFound;

    @Column(
        name = "time_found",
        nullable = true
    )
    private LocalTime timeFound;

    @Column(
        name = "item_type",
        nullable = false,
        length = 20
    )
    private String itemType; // lost or found

    @Column(
        nullable = false,
        length = 20
    )
    private String status; // active or resolved

    @Column(
        name = "contact_preference",
        nullable = false,
        length = 20
    )
    private String contactPreference; // public or private

    @Column(
        name = "views_count",
        nullable = false
    )
    private Integer viewsCount;

    @Column(
        name = "messages_count",
        nullable = false
    )
    private Integer messagesCount;

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
        name = "updated_at",
        nullable = true
    )
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Items() {} // initialize constructor

    public Items(Long itemId, Account account, String itemName, String description, String location, 
                 LocalDate dateFound, LocalTime timeFound, String itemType, String status, 
                 String contactPreference, Integer viewsCount, Integer messagesCount, 
                 LocalDateTime createdAt, LocalDateTime updatedAt) 
                 {
        this.itemId = itemId;
        this.account = account;
        this.itemName = itemName;
        this.description = description;
        this.location = location;
        this.dateFound = dateFound;
        this.timeFound = timeFound;
        this.itemType = itemType;
        this.status = status;
        this.contactPreference = contactPreference;
        this.viewsCount = viewsCount;
        this.messagesCount = messagesCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // @getters
    public Long getItemId() { return itemId; }
    public Account getAccount() { return account; }
    public String getItemName() { return itemName; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public LocalDate getDateFound() { return dateFound; }
    public LocalTime getTimeFound() { return timeFound; }
    public String getItemType() { return itemType; }
    public String getStatus() { return status; }
    public String getContactPreference() { return contactPreference; }
    public Integer getViewsCount() { return viewsCount; }
    public Integer getMessagesCount() { return messagesCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // @setters
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setAccount(Account account) { this.account = account; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location) { this.location = location; }
    public void setDateFound(LocalDate dateFound) { this.dateFound = dateFound; }
    public void setTimeFound(LocalTime timeFound) { this.timeFound = timeFound; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public void setStatus(String status) { this.status = status; }
    public void setContactPreference(String contactPreference) { this.contactPreference = contactPreference; }
    public void setViewsCount(Integer viewsCount) { this.viewsCount = viewsCount; }
    public void setMessagesCount(Integer messagesCount) { this.messagesCount = messagesCount; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

}
