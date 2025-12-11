package com.backend.server.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "item_images")

public class ItemImages {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne
    @JoinColumn(
        name = "items",
        referencedColumnName = "item_id",
        nullable = false
    )
    private Items item;

    @Column(
        name = "image_url",
        nullable = false,
        length = 500
    )
    private String imageUrl;

    @Column(
        name = "image_order",
        nullable = false
    )
    private Integer imageOrder;

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

    public ItemImages() {} // initialize constructor

    public ItemImages(Long imageId, Items item, String imageUrl, Integer imageOrder, LocalDateTime createdAt) {
        this.imageId = imageId;
        this.item = item;
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder;
        this.createdAt = createdAt;
    }

    // @getters
    public Long getImageId() { return imageId; }
    public Items getItem() { return item; }
    public String getImageUrl() { return imageUrl; }
    public Integer getImageOrder() { return imageOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // @setters
    public void setImageId(Long imageId) { this.imageId = imageId; }
    public void setItem(Items item) { this.item = item; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setImageOrder(Integer imageOrder) { this.imageOrder = imageOrder; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}

