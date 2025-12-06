package com.backend.server.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.server.dto.RequestSignUp;
import com.backend.server.models.Account;
import com.backend.server.models.Role;
import com.backend.server.models.User;
import com.backend.server.services.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody RequestSignUp request) {

        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPassword(request.getPassword());
        authService.registerAccount(account);

        Role role = new Role();
        role.setRoleName(request.getRole_name());
        role.setDescription(request.getDescription());

        User user = new User();
        user.setFullname(request.getFullname());
        user.setContactNumber(request.getContact_number());
        user.setAddress(request.getAddress());
        
        return ResponseEntity.status(HttpStatus.CREATED).body("Account successfully registered");
    }
    
}
