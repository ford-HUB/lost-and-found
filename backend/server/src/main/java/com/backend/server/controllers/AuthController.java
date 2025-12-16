package com.backend.server.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.server.dto.RequestLogin;
import com.backend.server.dto.RequestSignUp;
import com.backend.server.models.Account;
import com.backend.server.models.Role;
import com.backend.server.models.User;
import com.backend.server.services.AuthService;
import com.backend.server.services.JwtService;
import com.backend.server.services.UserAccountInfoService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserAccountInfoService userAccountInfoService;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager, JwtService jwtService, UserAccountInfoService userAccountInfoService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userAccountInfoService = userAccountInfoService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@jakarta.validation.Valid @RequestBody RequestSignUp request) {

        try {
            Account account = new Account();
            account.setEmail(request.getEmail());
            account.setPassword(request.getPassword());
            Account savedAccount = authService.registerAccount(account);

            Role role = new Role();
            role.setRoleName(request.getRoleName() != null ? request.getRoleName() : "USER");
            role.setDescription(request.getDescription() != null ? request.getDescription() : "Regular user account");
            role.setAccount(savedAccount);
            authService.registerRole(role);

            User user = new User();
            user.setFullname(request.getFullname());
            user.setContactNumber(request.getContact_number());
            user.setAddress(request.getAddress());
            user.setAccount(savedAccount);
            authService.registerUserInfo(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Account successfully registered");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
                
        } catch (Exception e) {
            Map<String, String> responseErr = new HashMap<>();
            responseErr.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseErr);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@jakarta.validation.Valid @RequestBody RequestLogin requestLogin) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestLogin.getEmail(), requestLogin.getPassword())
            );
    
            UserDetails userDetails = userAccountInfoService.loadUserByUsername(requestLogin.getEmail());
    
            String token = jwtService.generateToken(userDetails.getUsername());
            
            long expirationMilis = (requestLogin.getRememberMe() != null && requestLogin.getRememberMe()) 
                ? 1000L * 60 * 60 * 24 * 7 
                : 1000L * 60 * 30;
            int maxAgeSeconds = (int) (expirationMilis / 1000);

            ResponseCookie cookie = ResponseCookie.from("jwt", token)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(maxAgeSeconds)
            .sameSite("Lax")
            .build();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Account successfully LoggedIn");
            response.put("token", token); // Include token in response for localStorage storage
            return ResponseEntity.status(HttpStatus.ACCEPTED).header("Set-Cookie", cookie.toString()).body(response);
        } catch (Exception e) {
            Map<String, String> responseError = new HashMap<>();
            responseError.put("message", "Internal Server Error"+e.getMessage());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseError);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        try {
            ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Account successfully logged out");
            return ResponseEntity.status(HttpStatus.OK).header("Set-Cookie", cookie.toString()).body(response);
        } catch (Exception e) {
            Map<String, String> responseError = new HashMap<>();
            responseError.put("message", "Internal Server Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
        }
    }
    
}
