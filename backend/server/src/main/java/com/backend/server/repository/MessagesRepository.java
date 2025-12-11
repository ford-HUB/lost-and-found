package com.backend.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.server.models.Messages;

public interface MessagesRepository extends JpaRepository<Messages, Long> {
    
    // @find messages by conversation
    List<Messages> findByConversation_ConversationId(Long conversationId);
    
    // @find messages by conversation ordered by created date
    List<Messages> findByConversation_ConversationIdOrderByCreatedAtAsc(Long conversationId);
    
    // @find unread messages by conversation
    List<Messages> findByConversation_ConversationIdAndIsReadFalse(Long conversationId);
    
    // @count unread messages by conversation
    Long countByConversation_ConversationIdAndIsReadFalse(Long conversationId);
}
