package com.backend.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.server.models.ItemImages;

public interface ItemImagesRepository extends JpaRepository<ItemImages, Long> {
    
    // @find images by item
    List<ItemImages> findByItem_ItemId(Long itemId);
    
    // @find images by item ordered by image order
    List<ItemImages> findByItem_ItemIdOrderByImageOrderAsc(Long itemId);
}
