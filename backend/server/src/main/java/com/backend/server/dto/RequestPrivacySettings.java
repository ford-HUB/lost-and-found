package com.backend.server.dto;

import com.backend.server.validation.ValidProfileVisibility;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestPrivacySettings {
    @NotNull(message = "Profile visibility is required")
    @ValidProfileVisibility
    private String profileVisibility; // public, items-only, private
    
    @NotNull(message = "Show email setting is required")
    private Boolean showEmail;
    
    @NotNull(message = "Show phone setting is required")
    private Boolean showPhone;
    
    @NotNull(message = "Show in search setting is required")
    private Boolean showInSearch;
    
    @NotNull(message = "Allow messages setting is required")
    private Boolean allowMessages;
}

