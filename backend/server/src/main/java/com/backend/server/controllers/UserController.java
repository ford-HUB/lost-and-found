package com.backend.server.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

import com.backend.server.dto.RequestChangePassword;
import com.backend.server.dto.RequestDeleteAccount;
import com.backend.server.dto.RequestPrivacySettings;
import com.backend.server.dto.RequestUpdateProfile;
import com.backend.server.models.User;
import com.backend.server.security.UserAccountInfoDetails;
import com.backend.server.services.UserService;

@RestController
@RequestMapping("/api/auth/user")
@Validated
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAuthentication() {
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

            UserAccountInfoDetails user = (UserAccountInfoDetails) principal;
            Map<String, Object> response = new HashMap<>();
            response.put("email", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "check auth failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseErr);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
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
            
            User user = userService.getUserByEmail(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getUserId());
            response.put("fullname", user.getFullname());
            response.put("email", user.getAccount().getEmail());
            response.put("contactNumber", user.getContactNumber());
            response.put("address", user.getAddress());
            response.put("bio", user.getBio());
            response.put("avatarUrl", user.getAvatarUrl());
            response.put("createdAt", user.getAccount().getCreatedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to get profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@jakarta.validation.Valid @RequestBody RequestUpdateProfile request) {
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
            
            User user = userService.updateUserProfile(
                email, 
                request.getFullname(), 
                request.getContactNumber(), 
                request.getAddress(), 
                request.getBio()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getUserId());
            response.put("fullname", user.getFullname());
            response.put("email", user.getAccount().getEmail());
            response.put("contactNumber", user.getContactNumber());
            response.put("address", user.getAddress());
            response.put("bio", user.getBio());
            response.put("avatarUrl", user.getAvatarUrl());
            response.put("createdAt", user.getAccount().getCreatedAt());
            response.put("message", "Profile updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to update profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        }
    }

    @PostMapping("/profile/avatar")
    public ResponseEntity<?> uploadAvatar(
            @com.backend.server.validation.ValidImageFile @RequestParam("file") MultipartFile file) {
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
            
            User user = userService.updateUserAvatar(email, file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("avatarUrl", user.getAvatarUrl());
            response.put("message", "Avatar uploaded successfully");
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to upload avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to upload avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        }
    }

    @PutMapping("/profile/password")
    public ResponseEntity<?> changePassword(@jakarta.validation.Valid @RequestBody RequestChangePassword request) {
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
            
            userService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", e.getMessage());
            HttpStatus status = e.getMessage().contains("incorrect") || e.getMessage().contains("not found") 
                               ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(responseErr);
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to change password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getPublicProfile(
            @jakarta.validation.constraints.Positive(message = "User ID must be positive") @PathVariable Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String viewerEmail = null;
            
            // Get viewer email if authenticated
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserAccountInfoDetails) {
                    UserAccountInfoDetails userDetails = (UserAccountInfoDetails) principal;
                    viewerEmail = userDetails.getUsername();
                }
            }
            
            Map<String, Object> profile = userService.getPublicProfile(userId, viewerEmail);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", e.getMessage());
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : 
                               e.getMessage().contains("private") ? HttpStatus.FORBIDDEN : 
                               HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(responseErr);
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to get profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        }
    }

    @GetMapping("/profile/privacy")
    public ResponseEntity<?> getPrivacySettings() {
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
            
            Map<String, Object> settings = userService.getPrivacySettings(email);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to get privacy settings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        }
    }

    @PutMapping("/profile/privacy")
    public ResponseEntity<?> updatePrivacySettings(@jakarta.validation.Valid @RequestBody RequestPrivacySettings request) {
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
            
            Map<String, Object> settings = userService.updatePrivacySettings(
                email,
                request.getProfileVisibility(),
                request.getShowEmail(),
                request.getShowPhone(),
                request.getShowInSearch(),
                request.getAllowMessages()
            );
            
            return ResponseEntity.ok(settings);
        } catch (RuntimeException e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseErr);
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to update privacy settings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        }
    }

    @DeleteMapping("/account/delete")
    public ResponseEntity<?> deleteAccount(@jakarta.validation.Valid @RequestBody RequestDeleteAccount request) {
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
            
            userService.deleteAccount(email, request.getPassword());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Account deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", e.getMessage());
            HttpStatus status = e.getMessage().contains("incorrect") || e.getMessage().contains("not found") 
                               ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(responseErr);
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to delete account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        }
    }
}
