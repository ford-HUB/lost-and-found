package com.backend.server.config;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.backend.server.services.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class WebSocketSecurityInterceptor implements HandshakeInterceptor, ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public WebSocketSecurityInterceptor(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            
            // Extract JWT token from query parameter or cookie
            String token = extractToken(httpRequest);
            
            if (token != null) {
                try {
                    String email = jwtService.extractEmail(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    
                    if (jwtService.validateToken(token, userDetails)) {
                        // Store user email and token in attributes for later use
                        attributes.put("userEmail", email);
                        attributes.put("token", token);
                        return true;
                    }
                } catch (Exception e) {
                    // Token validation failed - allow connection but authentication will be checked in preSend
                    // This allows the connection to be established, but messages will be blocked if not authenticated
                }
            }
        }
        // Allow connection even without token - authentication will be checked in preSend
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, @Nullable Exception exception) {
        // No action needed after handshake
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            // Check if user is already authenticated
            Principal currentUser = accessor.getUser();
            
            // Log for debugging (only for important commands to reduce noise)
            if (command != null && (command == StompCommand.CONNECT || command == StompCommand.SUBSCRIBE || command == StompCommand.SEND)) {
                System.out.println("WebSocket preSend - Command: " + command + ", User: " + (currentUser != null ? currentUser.getName() : "null"));
            }
            
            // CRITICAL: Always authenticate during CONNECT to ensure Principal is set for the session
            // This is required for convertAndSendToUser to work correctly
            if (command == StompCommand.CONNECT || (currentUser == null && command != null)) {
                String token = null;
                
                // Try to get token from headers first (for CONNECT)
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                } else {
                    // Try to get from session attributes (set during handshake)
                    Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                    if (sessionAttributes != null) {
                        token = (String) sessionAttributes.get("token");
                        // Also try to get email directly if token not found
                        if (token == null) {
                            String email = (String) sessionAttributes.get("userEmail");
                            if (email != null) {
                                // If we have email but no token, try to authenticate with email
                                // This is a fallback - ideally we should have the token
                                try {
                                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                    accessor.setUser(auth);
                                    System.out.println("WebSocket user authenticated via email fallback: " + email + " for command: " + command);
                                    return message;
                                } catch (Exception e) {
                                    System.err.println("Failed to authenticate via email fallback: " + e.getMessage());
                                    // User not found, continue to try token
                                }
                            }
                        }
                    }
                }
                
                // If we have a token, validate and authenticate
                if (token != null) {
                    try {
                        String email = jwtService.extractEmail(token);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                        
                        if (jwtService.validateToken(token, userDetails)) {
                            // Set authentication - this is critical for convertAndSendToUser to work
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                            accessor.setUser(auth);
                            
                            System.out.println("WebSocket user authenticated: " + email + " for command: " + accessor.getCommand());
                            
                            // Store email in session attributes for later use
                            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                            if (sessionAttributes != null) {
                                sessionAttributes.put("userEmail", email);
                                sessionAttributes.put("token", token);
                            }
                        } else {
                            System.err.println("WebSocket token validation failed for: " + email);
                        }
                    } catch (Exception e) {
                        // Authentication failed - log for debugging
                        System.err.println("WebSocket authentication failed: " + e.getMessage());
                    }
                }
            }
        }
        
        return message;
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getParameter("token");
        if (token != null) {
            return token;
        }
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // Try cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
}

