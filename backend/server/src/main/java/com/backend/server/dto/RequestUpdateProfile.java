package com.backend.server.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestUpdateProfile {
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullname;
    
    @Size(max = 20, message = "Contact number must not exceed 20 characters")
    private String contactNumber;
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
}

