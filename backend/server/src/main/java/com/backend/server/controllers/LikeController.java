package com.backend.server.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.backend.server.security.UserAccountInfoDetails;
import com.backend.server.services.LikeService;

import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/auth/like")
@Validated
public class LikeController {

    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/{itemId}")
    public ResponseEntity<?> toggleLike(@Positive(message = "Item ID must be positive") @PathVariable Long itemId) {
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

            boolean isLiked = likeService.toggleLike(email, itemId);
            Long likeCount = likeService.getLikeCount(itemId);

            Map<String, Object> response = new HashMap<>();
            response.put("isLiked", isLiked);
            response.put("likeCount", likeCount);
            response.put("message", isLiked ? "Item liked successfully" : "Item unliked successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", e.getMessage());
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : 
                               HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(responseErr);
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to toggle like: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        }
    }
}

