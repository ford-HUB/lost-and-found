package com.backend.server.controllers;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.backend.server.dto.ConversationDTO;
import com.backend.server.dto.MessageDTO;
import com.backend.server.dto.SendMessageRequest;
import com.backend.server.services.ConversationService;
import com.backend.server.services.MessageService;

import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/auth/messages")
@Validated
public class MessageController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    public MessageController(ConversationService conversationService, MessageService messageService) {
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    @GetMapping("/conversations")
    public ResponseEntity<Map<String, Object>> getConversations(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = getEmailFromPrincipal(principal);
            List<ConversationDTO> conversations = conversationService.getConversationsForUser(email);
            response.put("success", true);
            response.put("conversations", conversations);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get conversations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<Map<String, Object>> getConversation(
            @Positive(message = "Conversation ID must be positive") @PathVariable Long conversationId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = getEmailFromPrincipal(principal);
            ConversationDTO conversation = conversationService.getConversationById(conversationId, email);
            
            // Mark messages as read when user opens conversation
            messageService.markMessagesAsRead(conversationId, email);
            
            response.put("success", true);
            response.put("conversation", conversation);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : 
                               e.getMessage().contains("Unauthorized") ? HttpStatus.FORBIDDEN : 
                               HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get conversation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/conversations/create")
    public ResponseEntity<Map<String, Object>> createConversation(
            @RequestBody Map<String, Object> request, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = getEmailFromPrincipal(principal);
            Long receiverAccountId = Long.parseLong(request.get("receiverAccountId").toString());
            Long itemId = Long.parseLong(request.get("itemId").toString());
            
            var conversation = conversationService.getOrCreateConversation(email, receiverAccountId, itemId);
            ConversationDTO conversationDTO = conversationService.getConversationById(
                conversation.getConversationId(), email);
            
            response.put("success", true);
            response.put("conversation", conversationDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create conversation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @Positive(message = "Conversation ID must be positive") @PathVariable Long conversationId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = getEmailFromPrincipal(principal);
            messageService.markMessagesAsRead(conversationId, email);
            response.put("success", true);
            response.put("message", "Messages marked as read");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to mark messages as read: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @MessageMapping("/message.send")
    public MessageDTO sendMessage(@jakarta.validation.Valid @Payload SendMessageRequest request, Principal principal) {
        System.out.println("=== MESSAGE RECEIVED VIA WEBSOCKET ===");
        System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));
        System.out.println("Request: " + request);
        
        String email = getEmailFromPrincipal(principal);
        System.out.println("Extracted email: " + email);
        
        MessageDTO result = messageService.sendMessage(
            request.getConversationId(),
            request.getMessageText(),
            email
        );
        
        System.out.println("Message processed, returning DTO: " + result);
        return result;
    }

    /**
     * Helper method to extract email from Principal
     */
    private String getEmailFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        if (principal instanceof Authentication) {
            Authentication auth = (Authentication) principal;
            if (auth.getPrincipal() instanceof UserDetails) {
                return ((UserDetails) auth.getPrincipal()).getUsername();
            }
        }
        
        return principal.getName();
    }
}

