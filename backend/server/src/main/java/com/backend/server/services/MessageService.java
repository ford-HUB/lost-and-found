package com.backend.server.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.server.dto.MessageDTO;
import com.backend.server.models.Account;
import com.backend.server.models.Conversations;
import com.backend.server.models.Messages;
import com.backend.server.models.User;
import com.backend.server.repository.AccountRepository;
import com.backend.server.repository.ConversationsRepository;
import com.backend.server.repository.MessagesRepository;
import com.backend.server.repository.UserRepository;

@Service
public class MessageService {

    private final MessagesRepository messagesRepository;
    private final ConversationsRepository conversationsRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;

    public MessageService(MessagesRepository messagesRepository,
                         ConversationsRepository conversationsRepository,
                         AccountRepository accountRepository,
                         UserRepository userRepository,
                         SimpMessagingTemplate messagingTemplate,
                         SimpUserRegistry userRegistry) {
        this.messagesRepository = messagesRepository;
        this.conversationsRepository = conversationsRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRegistry = userRegistry;
    }

    // Send a message and broadcast it via WebSocket

    @Transactional
    public MessageDTO sendMessage(Long conversationId, String messageText, String senderEmail) {
        Optional<Account> senderAccountOpt = accountRepository.findByEmail(senderEmail);
        if (senderAccountOpt.isEmpty()) {
            throw new RuntimeException("Sender account not found for email: " + senderEmail);
        }
        Account senderAccount = senderAccountOpt.get();

        Optional<User> senderUserOpt = userRepository.findByAccount(senderAccount);
        if (senderUserOpt.isEmpty()) {
            throw new RuntimeException("User not found for account: " + senderEmail);
        }
        User senderUser = senderUserOpt.get();

        // Get conversation
        Optional<Conversations> conversationOpt = conversationsRepository.findById(conversationId);
        if (conversationOpt.isEmpty()) {
            throw new RuntimeException("Conversation not found with id: " + conversationId);
        }
        Conversations conversation = conversationOpt.get();

        // Verify sender is part of conversation
        if (!conversation.getSender().getAccountId().equals(senderAccount.getAccountId()) &&
            !conversation.getReceiver().getAccountId().equals(senderAccount.getAccountId())) {
            throw new RuntimeException("Unauthorized: User is not part of this conversation");
        }

        // Create message
        Messages message = new Messages();
        message.setConversation(conversation);
        message.setSender(senderUser);
        message.setMessageText(messageText);
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());

        Messages savedMessage = messagesRepository.save(message);

        // Update conversation's last message time
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationsRepository.save(conversation);

        // Convert to DTO
        MessageDTO messageDTO = convertToDTO(savedMessage);

        // Determine receiver account ID for WebSocket routing
        Long receiverAccountId = conversation.getSender().getAccountId().equals(senderAccount.getAccountId())
            ? conversation.getReceiver().getAccountId()
            : conversation.getSender().getAccountId();

        // Get receiver account email for WebSocket destination
        Optional<Account> receiverAccountOpt = accountRepository.findById(receiverAccountId);
        if (receiverAccountOpt.isPresent()) {
            String receiverEmail = receiverAccountOpt.get().getEmail();
            
            try {
                // Check if user is registered in WebSocket sessions
                var user = userRegistry.getUser(receiverEmail);
                if (user != null) {
                    System.out.println("✅ User " + receiverEmail + " is registered with " + user.getSessions().size() + " session(s)");
                    user.getSessions().forEach(session -> {
                        System.out.println("  - Session ID: " + session.getId() + ", Subscriptions: " + session.getSubscriptions().size());
                    });
                } else {
                    System.err.println("⚠️ User " + receiverEmail + " is NOT registered in WebSocket sessions!");
                    System.err.println("Available users: " + userRegistry.getUsers().stream()
                        .map(u -> u.getName())
                        .toList());
                }
                
                // convertAndSendToUser routes to /user/{username}/queue/messages
                // The username parameter must match Principal.getName() in the WebSocket session
                messagingTemplate.convertAndSendToUser(
                    receiverEmail,
                    "/queue/messages",
                    messageDTO
                );
                System.out.println("✅ Message sent via convertAndSendToUser to: /user/" + receiverEmail + "/queue/messages");
            } catch (Exception e) {
                System.err.println("❌ Error sending message to receiver: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Receiver account not found for accountId: " + receiverAccountId);
        }

        // Also send to sender (for confirmation)
        System.out.println("=== SENDING MESSAGE TO SENDER ===");
        System.out.println("Sender email: " + senderEmail);
        System.out.println("Destination will be: /user/" + senderEmail + "/queue/messages");
        
        messagingTemplate.convertAndSendToUser(senderEmail, "/queue/messages", messageDTO);
        System.out.println("Message sent via convertAndSendToUser to: /user/" + senderEmail + "/queue/messages");

        return messageDTO;
    }

    /**
     * Mark messages as read
     */
    @Transactional
    public void markMessagesAsRead(Long conversationId, String userEmail) {
        Optional<Account> accountOpt = accountRepository.findByEmail(userEmail);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Account not found for email: " + userEmail);
        }
        Account account = accountOpt.get();

        Optional<User> userOpt = userRepository.findByAccount(account);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found for account: " + userEmail);
        }
        User user = userOpt.get();

        Optional<Conversations> conversationOpt = conversationsRepository.findById(conversationId);
        if (conversationOpt.isEmpty()) {
            throw new RuntimeException("Conversation not found with id: " + conversationId);
        }
        Conversations conversation = conversationOpt.get();

        // Verify user is part of conversation
        if (!conversation.getSender().getAccountId().equals(account.getAccountId()) &&
            !conversation.getReceiver().getAccountId().equals(account.getAccountId())) {
            throw new RuntimeException("Unauthorized: User is not part of this conversation");
        }

        // Get all unread messages in this conversation that are not from the current user
        java.util.List<Messages> unreadMessages = messagesRepository
            .findByConversation_ConversationIdAndIsReadFalse(conversationId);

        // Mark as read only messages from the other user
        for (Messages message : unreadMessages) {
            if (!message.getSender().getUserId().equals(user.getUserId())) {
                message.setIsRead(true);
                messagesRepository.save(message);
            }
        }
    }

    /**
     * Convert Messages entity to DTO
     */
    private MessageDTO convertToDTO(Messages message) {
        MessageDTO dto = new MessageDTO();
        dto.setMessageId(message.getMessageId());
        dto.setConversationId(message.getConversation().getConversationId());
        dto.setSenderId(message.getSender().getUserId());
        dto.setSenderName(message.getSender().getFullname());
        dto.setSenderAvatar(message.getSender().getAvatarUrl() != null ? message.getSender().getAvatarUrl() : 
            "https://ui-avatars.com/api/?name=" + message.getSender().getFullname().replace(" ", "+") + "&background=000000&color=ffffff&size=128");
        dto.setMessageText(message.getMessageText());
        dto.setIsRead(message.getIsRead());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}

