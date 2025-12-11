package com.backend.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.server.models.Likes;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    
    // @find likes by item
    List<Likes> findByItem_ItemId(Long itemId);
    
    // @find likes by account
    List<Likes> findByAccount_AccountId(Long accountId);
    
    // @check if account liked item
    Optional<Likes> findByAccount_AccountIdAndItem_ItemId(Long accountId, Long itemId);
    
    // @count likes by item
    Long countByItem_ItemId(Long itemId);
    
    // @check if account liked item (boolean)
    boolean existsByAccount_AccountIdAndItem_ItemId(Long accountId, Long itemId);
}
