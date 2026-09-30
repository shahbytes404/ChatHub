package com.shahbytes.chathub.config;

import com.shahbytes.chathub.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        var accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );

        if (accessor == null
                || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        var authorization = accessor.getFirstNativeHeader(
                HttpHeaders.AUTHORIZATION
        );

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException(
                    "WebSocket Authorization header is required"
            );
        }

        var principal = jwtService.parse(authorization.substring(7));

        accessor.setUser(principal);

        return message;
    }

}
