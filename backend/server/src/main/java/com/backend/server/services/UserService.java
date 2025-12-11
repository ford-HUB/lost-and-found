package com.backend.server.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.backend.server.models.Account;
import com.backend.server.models.Conversations;
import com.backend.server.models.ItemImages;
import com.backend.server.models.Items;
import com.backend.server.models.Likes;
import com.backend.server.models.Messages;
import com.backend.server.models.Role;
import com.backend.server.models.User;
import com.backend.server.models.UserSettings;
import com.backend.server.repository.AccountRepository;
import com.backend.server.repository.ConversationsRepository;
import com.backend.server.repository.ItemImagesRepository;
import com.backend.server.repository.ItemsRepository;
import com.backend.server.repository.LikesRepository;
import com.backend.server.repository.MessagesRepository;
import com.backend.server.repository.RoleRepository;
import com.backend.server.repository.UserRepository;
import com.backend.server.repository.UserSettingsRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CloudinaryService cloudinaryService;
    private final UserSettingsRepository userSettingsRepository;
    private final ItemsRepository itemsRepository;
    private final ItemImagesRepository itemImagesRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ConversationsRepository conversationsRepository;
    private final MessagesRepository messagesRepository;
    private final LikesRepository likesRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository, AccountRepository accountRepository, 
                       CloudinaryService cloudinaryService, UserSettingsRepository userSettingsRepository,
                       ItemsRepository itemsRepository, ItemImagesRepository itemImagesRepository,
                       BCryptPasswordEncoder passwordEncoder, ConversationsRepository conversationsRepository,
                       MessagesRepository messagesRepository, LikesRepository likesRepository,
                       RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.cloudinaryService = cloudinaryService;
        this.userSettingsRepository = userSettingsRepository;
        this.itemsRepository = itemsRepository;
        this.itemImagesRepository = itemImagesRepository;
        this.passwordEncoder = passwordEncoder;
        this.conversationsRepository = conversationsRepository;
        this.messagesRepository = messagesRepository;
        this.likesRepository = likesRepository;
        this.roleRepository = roleRepository;
    }

    public User getUserByEmail(String email) {
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Account not found for email: " + email);
        }
        Account account = accountOpt.get();
        Optional<User> userOpt = userRepository.findByAccount(account);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found for account: " + email);
        }
        return userOpt.get();
    }

    public User updateUserProfile(String email, String fullname, String contactNumber, String address, String bio) {
        User user = getUserByEmail(email);
        if (fullname != null && !fullname.isEmpty()) {
            user.setFullname(fullname);
        }
        if (contactNumber != null && !contactNumber.isEmpty()) {
            user.setContactNumber(contactNumber);
        }
        if (address != null && !address.isEmpty()) {
            user.setAddress(address);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        return userRepository.save(user);
    }

    public User updateUserAvatar(String email, MultipartFile file) throws IOException {
        User user = getUserByEmail(email);
        
        // Delete old avatar if exists
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                cloudinaryService.deleteImage(user.getAvatarUrl());
            } catch (Exception e) {
                // Log error but continue with upload
                System.err.println("Error deleting old avatar: " + e.getMessage());
            }
        }
        
        // Upload new avatar
        String avatarUrl = cloudinaryService.uploadImage(file, "profile-avatars");
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }

    public void changePassword(String email, String currentPassword, String newPassword) {
        // Get user account
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Account not found for email: " + email);
        }
        Account account = accountOpt.get();
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, account.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Validate new password
        if (newPassword == null || newPassword.isEmpty()) {
            throw new RuntimeException("New password cannot be empty");
        }
        
        if (newPassword.length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters long");
        }
        
        // Update password
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public Map<String, Object> getPublicProfile(Long userId, String viewerEmail) {
        // Get user by userId
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = userOpt.get();
        Account account = user.getAccount();
        
        // Get user settings
        Optional<UserSettings> settingsOpt = userSettingsRepository.findByAccount_AccountId(account.getAccountId());
        UserSettings settings = settingsOpt.orElse(null);
        
        // Check if viewer is the profile owner
        boolean isOwner = false;
        if (viewerEmail != null) {
            Optional<Account> viewerAccountOpt = accountRepository.findByEmail(viewerEmail);
            if (viewerAccountOpt.isPresent() && viewerAccountOpt.get().getAccountId().equals(account.getAccountId())) {
                isOwner = true;
            }
        }
        
        // Check profile visibility
        String profileVisibility = settings != null ? settings.getProfileVisibility() : "public";
        if (!isOwner && "private".equals(profileVisibility)) {
            throw new RuntimeException("Profile is private");
        }
        
        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("accountId", account.getAccountId()); // Add accountId for messaging
        response.put("fullname", user.getFullname());
        response.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : 
            "https://ui-avatars.com/api/?name=" + user.getFullname().replace(" ", "+") + "&background=000000&color=ffffff&size=200");
        response.put("createdAt", account.getCreatedAt());
        
        // For "items-only" mode, hide all profile info from non-owners
        // For "public" mode, respect individual settings
        if (isOwner || "public".equals(profileVisibility)) {
            // Show email if settings allow or if owner
            boolean showEmail = isOwner || (settings != null && settings.getShowEmail());
            if (showEmail) {
                response.put("email", account.getEmail());
            }
            
            // Show phone if settings allow or if owner
            boolean showPhone = isOwner || (settings != null && settings.getShowPhone());
            if (showPhone) {
                response.put("contactNumber", user.getContactNumber());
            }
            
            // Show address and bio for public profiles or owners
            response.put("address", user.getAddress());
            response.put("bio", user.getBio());
        }
        // For "items-only" mode, non-owners only see basic info (name, avatar) and items
        
        // Always include items array (even if empty) for messaging purposes
        // Show items if profile is public or items-only, or if viewer is owner
        List<Items> items = itemsRepository.findByAccount_AccountIdAndStatus(account.getAccountId(), "active");
        List<Map<String, Object>> itemsList = new ArrayList<>();
        
        if (isOwner || "public".equals(profileVisibility) || "items-only".equals(profileVisibility)) {
            for (Items item : items) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("itemId", item.getItemId());
                itemData.put("itemName", item.getItemName());
                itemData.put("description", item.getDescription());
                itemData.put("location", item.getLocation());
                itemData.put("itemType", item.getItemType());
                itemData.put("dateFound", item.getDateFound());
                itemData.put("timeFound", item.getTimeFound());
                itemData.put("createdAt", item.getCreatedAt());
                
                // Get images
                List<ItemImages> images = itemImagesRepository.findByItem_ItemIdOrderByImageOrderAsc(item.getItemId());
                List<String> imageUrls = new ArrayList<>();
                for (ItemImages img : images) {
                    imageUrls.add(img.getImageUrl());
                }
                itemData.put("images", imageUrls);
                
                itemsList.add(itemData);
            }
        }
        // Always put items array (even if empty) so frontend can handle it
        response.put("items", itemsList);
        
        // Add privacy settings info
        response.put("profileVisibility", profileVisibility);
        response.put("allowMessages", settings != null ? settings.getAllowMessages() : true);
        response.put("isOwner", isOwner);
        
        return response;
    }

    public Map<String, Object> getPrivacySettings(String email) {
        User user = getUserByEmail(email);
        Account account = user.getAccount();
        
        Optional<UserSettings> settingsOpt = userSettingsRepository.findByAccount_AccountId(account.getAccountId());
        UserSettings settings;
        
        // Create default settings if they don't exist
        if (settingsOpt.isEmpty()) {
            settings = new UserSettings();
            settings.setAccount(account);
            settings.setProfileVisibility("public");
            settings.setShowEmail(true);
            settings.setShowPhone(true);
            settings.setShowInSearch(true);
            settings.setAllowMessages(true);
            settings.setUpdatedAt(LocalDateTime.now());
            settings = userSettingsRepository.save(settings);
        } else {
            settings = settingsOpt.get();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("profileVisibility", settings.getProfileVisibility());
        response.put("showEmail", settings.getShowEmail());
        response.put("showPhone", settings.getShowPhone());
        response.put("showInSearch", settings.getShowInSearch());
        response.put("allowMessages", settings.getAllowMessages());
        
        return response;
    }

    public Map<String, Object> updatePrivacySettings(String email, String profileVisibility, 
                                                      Boolean showEmail, Boolean showPhone, 
                                                      Boolean showInSearch, Boolean allowMessages) {
        User user = getUserByEmail(email);
        Account account = user.getAccount();
        
        Optional<UserSettings> settingsOpt = userSettingsRepository.findByAccount_AccountId(account.getAccountId());
        UserSettings settings;
        
        // Create settings if they don't exist
        if (settingsOpt.isEmpty()) {
            settings = new UserSettings();
            settings.setAccount(account);
        } else {
            settings = settingsOpt.get();
        }
        
        // Update settings
        if (profileVisibility != null && !profileVisibility.isEmpty()) {
            if (!profileVisibility.equals("public") && !profileVisibility.equals("items-only") && !profileVisibility.equals("private")) {
                throw new RuntimeException("Invalid profile visibility value. Must be 'public', 'items-only', or 'private'");
            }
            settings.setProfileVisibility(profileVisibility);
        }
        
        if (showEmail != null) {
            settings.setShowEmail(showEmail);
        }
        
        if (showPhone != null) {
            settings.setShowPhone(showPhone);
        }
        
        if (showInSearch != null) {
            settings.setShowInSearch(showInSearch);
        }
        
        if (allowMessages != null) {
            settings.setAllowMessages(allowMessages);
        }
        
        settings.setUpdatedAt(LocalDateTime.now());
        settings = userSettingsRepository.save(settings);
        
        Map<String, Object> response = new HashMap<>();
        response.put("profileVisibility", settings.getProfileVisibility());
        response.put("showEmail", settings.getShowEmail());
        response.put("showPhone", settings.getShowPhone());
        response.put("showInSearch", settings.getShowInSearch());
        response.put("allowMessages", settings.getAllowMessages());
        response.put("message", "Privacy settings updated successfully");
        
        return response;
    }

    public void deleteAccount(String email, String password) {
        // Get user account
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Account not found for email: " + email);
        }
        Account account = accountOpt.get();
        
        // Verify password
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new RuntimeException("Password is incorrect");
        }
        
        // Get user
        Optional<User> userOpt = userRepository.findByAccount(account);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found for account: " + email);
        }
        User user = userOpt.get();
        Long accountId = account.getAccountId();
        
        // 1. Get all conversations where account is sender or receiver
        List<Conversations> allConversations = conversationsRepository.findBySender_AccountIdOrReceiver_AccountId(accountId, accountId);
        
        // 2. Delete all messages in these conversations (to avoid foreign key constraints)
        // Since we're deleting the account, we delete all messages in conversations involving this account
        for (Conversations conversation : allConversations) {
            List<Messages> messages = messagesRepository.findByConversation_ConversationId(conversation.getConversationId());
            messagesRepository.deleteAll(messages);
        }
        
        // 3. Delete conversations where account is sender or receiver
        conversationsRepository.deleteAll(allConversations);
        
        // 4. Get all items for the account
        List<Items> items = itemsRepository.findByAccount_AccountId(accountId);
        
        // 5. For each item, delete item images and likes
        for (Items item : items) {
            // Delete item images from database and Cloudinary
            List<ItemImages> itemImages = itemImagesRepository.findByItem_ItemId(item.getItemId());
            for (ItemImages image : itemImages) {
                try {
                    cloudinaryService.deleteImage(image.getImageUrl());
                } catch (Exception e) {
                    // Log error but continue with deletion
                    System.err.println("Error deleting item image from Cloudinary: " + e.getMessage());
                }
                itemImagesRepository.delete(image);
            }
            
            // Delete likes for this item
            List<Likes> itemLikes = likesRepository.findByItem_ItemId(item.getItemId());
            likesRepository.deleteAll(itemLikes);
        }
        
        // 6. Delete all items
        itemsRepository.deleteAll(items);
        
        // 7. Delete any remaining likes by account (safety check)
        List<Likes> accountLikes = likesRepository.findByAccount_AccountId(accountId);
        likesRepository.deleteAll(accountLikes);
        
        // 8. Delete user settings
        Optional<UserSettings> settingsOpt = userSettingsRepository.findByAccount_AccountId(accountId);
        if (settingsOpt.isPresent()) {
            userSettingsRepository.delete(settingsOpt.get());
        }
        
        // 9. Delete role
        Optional<Role> roleOpt = roleRepository.findByAccount_AccountId(accountId);
        if (roleOpt.isPresent()) {
            roleRepository.delete(roleOpt.get());
        }
        
        // 10. Delete user avatar from Cloudinary
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                cloudinaryService.deleteImage(user.getAvatarUrl());
            } catch (Exception e) {
                // Log error but continue with deletion
                System.err.println("Error deleting avatar from Cloudinary: " + e.getMessage());
            }
        }
        
        // 11. Delete user
        userRepository.delete(user);
        
        // 12. Delete account
        accountRepository.delete(account);
    }
}
