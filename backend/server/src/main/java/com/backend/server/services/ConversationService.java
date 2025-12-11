package com.backend.server.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.server.dto.ConversationDTO;
import com.backend.server.dto.MessageDTO;
import com.backend.server.models.Account;
import com.backend.server.models.Conversations;
import com.backend.server.models.Items;
import com.backend.server.models.Messages;
import com.backend.server.models.User;
import com.backend.server.repository.AccountRepository;
import com.backend.server.repository.ConversationsRepository;
import com.backend.server.repository.ItemsRepository;
import com.backend.server.repository.MessagesRepository;
import com.backend.server.repository.UserRepository;

@Service
public class ConversationService {

    private final ConversationsRepository conversationsRepository;
    private final MessagesRepository messagesRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final ItemsRepository itemsRepository;

    public ConversationService(ConversationsRepository conversationsRepository,
                              MessagesRepository messagesRepository,
                              AccountRepository accountRepository,
                              UserRepository userRepository,
                              ItemsRepository itemsRepository) {
        this.conversationsRepository = conversationsRepository;
        this.messagesRepository = messagesRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.itemsRepository = itemsRepository;
    }

    // Get all conversations for a user (as sender or receiver)
    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversationsForUser(String email) {
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Account not found for email: " + email);
        }
        Account account = accountOpt.get();

        // Get all conversations where user is sender or receiver
        List<Conversations> conversations = conversationsRepository
            .findBySender_AccountIdOrReceiver_AccountId(account.getAccountId(), account.getAccountId());

        return conversations.stream()
            .map(conv -> convertToDTO(conv, account.getAccountId()))
            .collect(Collectors.toList());
    }

    //  Get or create a conversation between two users for an item
    @Transactional
    public Conversations getOrCreateConversation(String senderEmail, Long receiverAccountId, Long itemId) {
        Optional<Account> senderAccountOpt = accountRepository.findByEmail(senderEmail);
        if (senderAccountOpt.isEmpty()) {
            throw new RuntimeException("Sender account not found for email: " + senderEmail);
        }
        Account senderAccount = senderAccountOpt.get();

        Optional<Account> receiverAccountOpt = accountRepository.findById(receiverAccountId);
        if (receiverAccountOpt.isEmpty()) {
            throw new RuntimeException("Receiver account not found with id: " + receiverAccountId);
        }
        Account receiverAccount = receiverAccountOpt.get();

        // Check if conversation already exists
        Optional<Conversations> existingConv = conversationsRepository
            .findBySender_AccountIdAndReceiver_AccountIdAndItem_ItemId(
                senderAccount.getAccountId(), receiverAccountId, itemId);

        if (existingConv.isPresent()) {
            return existingConv.get();
        }

        // Also check reverse (receiver as sender, sender as receiver)
        Optional<Conversations> reverseConv = conversationsRepository
            .findBySender_AccountIdAndReceiver_AccountIdAndItem_ItemId(
                receiverAccountId, senderAccount.getAccountId(), itemId);

        if (reverseConv.isPresent()) {
            return reverseConv.get();
        }

        // Create new conversation
        Optional<Items> itemOpt = itemsRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new RuntimeException("Item not found with id: " + itemId);
        }
        Items item = itemOpt.get();

        Conversations conversation = new Conversations();
        conversation.setItem(item);
        conversation.setSender(senderAccount);
        conversation.setReceiver(receiverAccount);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setLastMessageAt(LocalDateTime.now());

        return conversationsRepository.save(conversation);
    }

    // Get conversation by ID (with authorization check)

    @Transactional(readOnly = true)
    public ConversationDTO getConversationById(Long conversationId, String email) {
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Account not found for email: " + email);
        }
        Account account = accountOpt.get();

        Optional<Conversations> conversationOpt = conversationsRepository.findById(conversationId);
        if (conversationOpt.isEmpty()) {
            throw new RuntimeException("Conversation not found with id: " + conversationId);
        }
        Conversations conversation = conversationOpt.get();

        // Check if user is part of this conversation
        if (!conversation.getSender().getAccountId().equals(account.getAccountId()) &&
            !conversation.getReceiver().getAccountId().equals(account.getAccountId())) {
            throw new RuntimeException("Unauthorized access to conversation");
        }

        ConversationDTO dto = convertToDTO(conversation, account.getAccountId());
        
        // Load messages for this conversation
        List<Messages> messages = messagesRepository
            .findByConversation_ConversationIdOrderByCreatedAtAsc(conversationId);
        List<MessageDTO> messageDTOs = messages.stream()
            .map(this::convertMessageToDTO)
            .collect(Collectors.toList());
        dto.setMessages(messageDTOs);

        return dto;
    }

    // Convert Conversations entity to DTO

    private ConversationDTO convertToDTO(Conversations conversation, Long currentAccountId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setConversationId(conversation.getConversationId());
        dto.setItemId(conversation.getItem().getItemId());
        dto.setItemName(conversation.getItem().getItemName());

        // Determine the "other" user
        Account otherAccount;
        if (conversation.getSender().getAccountId().equals(currentAccountId)) {
            otherAccount = conversation.getReceiver();
        } else {
            otherAccount = conversation.getSender();
        }

        Optional<User> otherUserOpt = userRepository.findByAccount(otherAccount);
        if (otherUserOpt.isPresent()) {
            User otherUser = otherUserOpt.get();
            dto.setOtherUserId(otherUser.getUserId());
            dto.setOtherUserName(otherUser.getFullname());
            dto.setOtherUserAvatar(otherUser.getAvatarUrl() != null ? otherUser.getAvatarUrl() : 
                "https://ui-avatars.com/api/?name=" + otherUser.getFullname().replace(" ", "+") + "&background=000000&color=ffffff&size=128");
        }

        // Load all messages once for this conversation
        List<Messages> messages = messagesRepository
            .findByConversation_ConversationIdOrderByCreatedAtAsc(conversation.getConversationId());

        // Get last message (if any)
        if (!messages.isEmpty()) {
            Messages lastMessage = messages.get(messages.size() - 1);
            dto.setLastMessage(lastMessage.getMessageText());
            dto.setLastMessageAt(lastMessage.getCreatedAt());
        } else {
            dto.setLastMessage("");
            dto.setLastMessageAt(conversation.getCreatedAt());
        }

        // Count unread messages for the CURRENT user only:
        // message is not marked as read
        // and it was sent by the OTHER user (sender account != currentAccountId)
        long unreadCount = messages.stream()
            .filter(m -> m.getIsRead() == null || !m.getIsRead())
            .filter(m -> {
                try {
                    return m.getSender() != null
                        && m.getSender().getAccount() != null
                        && !m.getSender().getAccount().getAccountId().equals(currentAccountId);
                } catch (Exception e) {
                    // In case of any missing data, don't count as unread
                    return false;
                }
            })
            .count();

        dto.setUnreadCount((int) unreadCount);

        return dto;
    }

    // Convert Messages entity to DTO
    
    private MessageDTO convertMessageToDTO(Messages message) {
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

