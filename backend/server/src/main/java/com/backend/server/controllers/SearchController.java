package com.backend.server.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.server.security.UserAccountInfoDetails;
import com.backend.server.services.SearchService;

@RestController
@RequestMapping("/api/auth/search-engine")
public class SearchController {

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchItems(@RequestParam(value = "q", required = false) String query) {
        try {
            // Get current user email if authenticated
            String currentUserEmail = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserAccountInfoDetails) {
                    UserAccountInfoDetails userDetails = (UserAccountInfoDetails) principal;
                    currentUserEmail = userDetails.getUsername();
                }
            }
            
            List<Map<String, Object>> results = searchService.searchItems(query, currentUserEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("results", results);
            response.put("count", results.size());
            response.put("query", query != null ? query : "");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to search items: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        }
    }

    @GetMapping("/search/hints")
    public ResponseEntity<?> getSearchHints(@RequestParam(value = "q", required = false) String query) {
        try {
            List<String> hints = searchService.getSearchHints(query);
            
            Map<String, Object> response = new HashMap<>();
            response.put("hints", hints);
            response.put("count", hints.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", "Failed to get search hints: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErr);
        }
    }
}

