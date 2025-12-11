package com.backend.server.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.server.models.Account;
import com.backend.server.models.ItemImages;
import com.backend.server.models.Items;
import com.backend.server.models.User;
import com.backend.server.models.UserSettings;
import com.backend.server.repository.AccountRepository;
import com.backend.server.repository.ItemImagesRepository;
import com.backend.server.repository.ItemsRepository;
import com.backend.server.repository.LikesRepository;
import com.backend.server.repository.UserRepository;
import com.backend.server.repository.UserSettingsRepository;

@Service
public class SearchService {
    private final ItemsRepository itemsRepository;
    private final ItemImagesRepository itemImagesRepository;
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final LikesRepository likesRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public SearchService(ItemsRepository itemsRepository, ItemImagesRepository itemImagesRepository,
                        UserRepository userRepository, UserSettingsRepository userSettingsRepository,
                        LikesRepository likesRepository, AccountRepository accountRepository) {
        this.itemsRepository = itemsRepository;
        this.itemImagesRepository = itemImagesRepository;
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.likesRepository = likesRepository;
        this.accountRepository = accountRepository;
    }

    public List<Map<String, Object>> searchItems(String query, String currentUserEmail) {
        List<Items> items;
        
        if (query == null || query.trim().isEmpty()) {
            items = itemsRepository.findByStatus("active");
        } else {
            items = itemsRepository.searchItems(query.trim());
        }

        // Build response with item details
        List<Map<String, Object>> results = new ArrayList<>();
        for (Items item : items) {
            Account account = item.getAccount();
            
            // Skip items without account
            if (account == null) {
                continue;
            }
            
            Optional<User> userOpt = userRepository.findByAccount(account);
            
            if (userOpt.isEmpty()) {
                continue;
            }
            
            User user = userOpt.get();
            
            // Get user settings to check profile visibility and search settings
            Optional<UserSettings> settingsOpt = userSettingsRepository.findByAccount_AccountId(account.getAccountId());
            String profileVisibility = "public"; // default
            Boolean showInSearch = true; // default
            if (settingsOpt.isPresent()) {
                UserSettings settings = settingsOpt.get();
                profileVisibility = settings.getProfileVisibility();
                showInSearch = settings.getShowInSearch();
            }
            
            boolean isOwner = false;
            if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
                Optional<Account> currentUserAccountOpt = accountRepository.findByEmail(currentUserEmail);
                if (currentUserAccountOpt.isPresent() && currentUserAccountOpt.get().getAccountId().equals(account.getAccountId())) {
                    isOwner = true;
                }
            }
            
            if (!isOwner && (showInSearch == null || !showInSearch)) {
                continue;
            }
            
            if (!isOwner && "private".equals(profileVisibility)) {
                continue;
            }
            
            // Get images for this item
            List<ItemImages> images = itemImagesRepository.findByItem_ItemIdOrderByImageOrderAsc(item.getItemId());
            List<String> imageUrls = new ArrayList<>();
            for (ItemImages img : images) {
                imageUrls.add(img.getImageUrl());
            }
            
            String relativeDate = formatRelativeDate(item.getCreatedAt());
            
            // Build item data
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("id", item.getItemId());
            itemData.put("userId", user.getUserId());
            itemData.put("userName", user.getFullname());
            itemData.put("userAvatar", user.getAvatarUrl() != null ? user.getAvatarUrl() : "https://ui-avatars.com/api/?name=" + user.getFullname().replace(" ", "+") + "&background=000000&color=ffffff&size=128");
            itemData.put("itemName", item.getItemName());
            itemData.put("description", item.getDescription());
            itemData.put("images", imageUrls);
            itemData.put("location", item.getLocation());
            itemData.put("date", relativeDate);
            itemData.put("type", item.getItemType());
            itemData.put("profileVisibility", profileVisibility);
            
            // Get like count
            Long likeCount = likesRepository.countByItem_ItemId(item.getItemId());
            itemData.put("likeCount", likeCount);
            
            // Check if current user liked this item
            boolean isLiked = false;
            if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
                Optional<Account> currentUserAccountOpt = accountRepository.findByEmail(currentUserEmail);
                if (currentUserAccountOpt.isPresent()) {
                    isLiked = likesRepository.existsByAccount_AccountIdAndItem_ItemId(
                        currentUserAccountOpt.get().getAccountId(), item.getItemId());
                }
            }
            itemData.put("isLiked", isLiked);
            
            results.add(itemData);
        }
        
        return results;
    }

    public List<String> getSearchHints(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Get distinct item names that match the query
        List<String> hints = itemsRepository.getItemNameHints(query.trim());
        
        // Limit to 10 hints
        return hints.size() > 10 ? hints.subList(0, 10) : hints;
    }

    private String formatRelativeDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Unknown";
        }
        
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);
        
        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days == 1 ? "1 day ago" : days + " days ago";
        } else if (hours > 0) {
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        } else if (minutes > 0) {
            return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
        } else {
            return "Just now";
        }
    }
}

