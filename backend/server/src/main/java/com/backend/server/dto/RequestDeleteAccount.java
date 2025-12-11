package com.backend.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestDeleteAccount {
    @NotBlank(message = "Password is required")
    private String password;
    
    private String confirmText; // Optional: for additional confirmation like "DELETE"
}

