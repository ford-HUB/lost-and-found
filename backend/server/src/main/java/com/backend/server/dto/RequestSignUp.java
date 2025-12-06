package com.backend.server.dto;

import lombok.*;

@Data
public class RequestSignUp {
    private String email;
    private String password;
    private String fullname;
    private String contact_number;
    private String address;
    private String role_name;
    private String description;
}
