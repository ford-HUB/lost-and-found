package com.backend.server.dto;

import com.backend.server.validation.ValidContactPreference;
import com.backend.server.validation.ValidItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestReportItem {
    @NotBlank(message = "Item type is required")
    @ValidItemType
    private String itemType; // lost or found
    
    @NotBlank(message = "Item name is required")
    @Size(min = 1, max = 255, message = "Item name must be between 1 and 255 characters")
    private String itemName;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;
    
    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;
    
    @NotBlank(message = "Date is required")
    private String date; // ISO date string
    
    private String time; // ISO time string (optional)
    
    @NotBlank(message = "Contact preference is required")
    @ValidContactPreference
    private String contactPreference; // public or private
}

