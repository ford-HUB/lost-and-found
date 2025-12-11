package com.backend.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.server.models.Items;

public interface ItemsRepository extends JpaRepository<Items, Long> {
    
    // @find items by account
    List<Items> findByAccount_AccountId(Long accountId);
    
    // @find items by status
    List<Items> findByStatus(String status);
    
    // @find items by type (lost or found)
    List<Items> findByItemType(String itemType);
    
    // @find items by account and status
    List<Items> findByAccount_AccountIdAndStatus(Long accountId, String status);
    
    // @find items by account and type
    List<Items> findByAccount_AccountIdAndItemType(Long accountId, String itemType);
    
    // @search items by query (item name, description, or location)
    @Query("SELECT i FROM Items i WHERE i.status = 'active' AND " +
           "(LOWER(i.itemName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.location) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY i.createdAt DESC")
    List<Items> searchItems(@Param("query") String query);
    
    // @get distinct item names for search hints
    @Query("SELECT DISTINCT i.itemName FROM Items i WHERE i.status = 'active' AND " +
           "LOWER(i.itemName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY i.itemName ASC")
    List<String> getItemNameHints(@Param("query") String query);
}
