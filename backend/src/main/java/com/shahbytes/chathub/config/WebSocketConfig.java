package com.shahbytes.chathub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final String[] allowedOrigins;
    private final WebSocketAuthInterceptor authInterceptor;

    public WebSocketConfig(
            @Value("${chathub.websocket.allow-origins}")
            String allowedOrigins, WebSocketAuthInterceptor authInterceptor) {
        this.allowedOrigins = allowedOrigins.split(",");
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry
    ) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry
    ) {
        // /app
        registry.setApplicationDestinationPrefixes("/app");

        registry.setUserDestinationPrefix("/user");

        registry.enableSimpleBroker(
                "/queue",
                "/topic");

        // /user/queue/events
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration
    ) {
        registration.interceptors(authInterceptor);
    }

}
