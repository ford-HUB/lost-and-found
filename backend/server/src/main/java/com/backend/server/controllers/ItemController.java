package com.backend.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

import com.backend.server.models.ItemImages;
import com.backend.server.models.Items;
import com.backend.server.security.UserAccountInfoDetails;
import com.backend.server.services.ItemService;
import com.backend.server.services.LikeService;
import com.backend.server.validation.ValidContactPreference;
import com.backend.server.validation.ValidImageFiles;
import com.backend.server.validation.ValidItemType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/auth/item")
@Validated
public class ItemController {

    private final ItemService itemService;
    private final LikeService likeService;

    @Autowired
    public ItemController(ItemService itemService, LikeService likeService) {
        this.itemService = itemService;
        this.likeService = likeService;
    }

    @PostMapping("/report")
    public ResponseEntity<?> reportItem(
            @ValidItemType @RequestParam("itemType") String itemType,
            @NotBlank(message = "Item name is required") 
            @Size(min = 1, max = 255, message = "Item name must be between 1 and 255 characters")
            @RequestParam("itemName") String itemName,
            @NotBlank(message = "Description is required")
            @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
            @RequestParam("description") String description,
            @NotBlank(message = "Location is required")
            @Size(max = 255, message = "Location must not exceed 255 characters")
            @RequestParam("location") String location,
            @NotBlank(message = "Date is required")
            @RequestParam("date") String date,
            @RequestParam(value = "time", required = false) String time,
            @ValidContactPreference @RequestParam("contactPreference") String contactPreference,
            @ValidImageFiles @RequestParam("images") List<MultipartFile> imageFiles) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> responseErr = new HashMap<>();
                responseErr.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseErr);
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserAccountInfoDetails)) {
                Map<String, String> responseErr = new HashMap<>();
                responseErr.put("message", "Invalid authentication principal");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseErr);
            }

            UserAccountInfoDetails userDetails = (UserAccountInfoDetails) principal;
            String email = userDetails.getUsername();

            Items item = itemService.reportItem(
                    email,
                    itemType,
                    itemName,
                    description,
                    location,
                    date,
                    time,
                    contactPreference,
                    imageFiles
            );

            Map<String, Object> response = new HashMap<>();
            response.put("itemId", item.getItemId());
            response.put("itemName", item.getItemName());
            response.put("itemType", item.getItemType());
            response.put("status", item.getStatus());
            response.put("message", "Item reported successfully");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to upload images: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to report item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        }
    }

    @GetMapping("/my-items")
    public ResponseEntity<?> getMyItems() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> responseErr = new HashMap<>();
                responseErr.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseErr);
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserAccountInfoDetails)) {
                Map<String, String> responseErr = new HashMap<>();
                responseErr.put("message", "Invalid authentication principal");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseErr);
            }

            UserAccountInfoDetails userDetails = (UserAccountInfoDetails) principal;
            String email = userDetails.getUsername();

            List<Items> items = itemService.getUserItems(email);
            
            // Build response with items and their images
            List<Map<String, Object>> itemsResponse = new ArrayList<>();
            for (Items item : items) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("id", item.getItemId());
                itemData.put("itemName", item.getItemName());
                itemData.put("description", item.getDescription());
                itemData.put("location", item.getLocation());
                itemData.put("date", item.getDateFound().toString());
                itemData.put("time", item.getTimeFound() != null ? item.getTimeFound().toString() : null);
                itemData.put("status", item.getStatus());
                itemData.put("type", item.getItemType());
                itemData.put("views", item.getViewsCount());
                itemData.put("messages", item.getMessagesCount());
                itemData.put("likes", likeService.getLikeCount(item.getItemId()));
                itemData.put("contactPreference", item.getContactPreference());
                itemData.put("createdAt", item.getCreatedAt().toString());
                
                // Get images for this item
                List<ItemImages> images = itemService.getItemImages(item.getItemId());
                List<String> imageUrls = new ArrayList<>();
                for (ItemImages img : images) {
                    imageUrls.add(img.getImageUrl());
                }
                itemData.put("images", imageUrls);
                
                itemsResponse.add(itemData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("items", itemsResponse);
            response.put("count", itemsResponse.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to get items: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        }
    }
}

