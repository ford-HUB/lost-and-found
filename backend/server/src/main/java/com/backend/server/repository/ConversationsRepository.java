package com.backend.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.server.models.Conversations;

public interface ConversationsRepository extends JpaRepository<Conversations, Long> {
    
    // @find conversation by item
    List<Conversations> findByItem_ItemId(Long itemId);
    
    // @find conversation by sender account
    List<Conversations> findBySender_AccountId(Long accountId);
    
    // @find conversation by receiver account
    List<Conversations> findByReceiver_AccountId(Long accountId);
    
    // @find conversation by both sender, receiver and item
    Optional<Conversations> findBySender_AccountIdAndReceiver_AccountIdAndItem_ItemId(
        Long senderAccountId, Long receiverAccountId, Long itemId);
    
    // @find all conversations for an account (as sender or receiver)
    List<Conversations> findBySender_AccountIdOrReceiver_AccountId(
        Long senderAccountId, Long receiverAccountId);
}
