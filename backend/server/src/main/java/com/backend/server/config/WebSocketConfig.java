package com.backend.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketSecurityInterceptor webSocketSecurityInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker to carry messages back to the client
        // on destinations prefixed with "/topic" and "/queue"
        // User-specific destinations use the same broker prefixes (typically "/queue")
        // and are resolved via the user destination prefix configured below.
        // We intentionally do NOT add "/user" here to avoid conflicts with Spring's
        // built-in user destination handling for convertAndSendToUser.
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages bound to methods annotated with @MessageMapping
        // Clients send messages to destinations prefixed with "/app"
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific destinations
        // Messages sent to "/user/{username}/queue/messages" will be routed to the specific user
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint for WebSocket connections
        // Clients will connect to: ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins pero not recommeded for production
                .addInterceptors(webSocketSecurityInterceptor)
                .withSockJS(); // Enable SockJS fallback options for browsers that don't support WebSocket
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add interceptor to authenticate WebSocket messages
        registration.interceptors(webSocketSecurityInterceptor);
    }
}

