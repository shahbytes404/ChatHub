package com.shahbytes.chathub.config;

import com.shahbytes.chathub.security.ChatPrincipal;
import com.shahbytes.chathub.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebSocketPresenceListener {
    private final PresenceService presenceService;

    private final ConcurrentHashMap<String, UUID> sessionUsers =
            new ConcurrentHashMap<>();

    @EventListener
    public void connected(SessionConnectedEvent event) {
        var accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() instanceof ChatPrincipal principal
                && accessor.getSessionId() != null) {
            var sessionId = accessor.getSessionId();
            var userId = principal.userId();

            sessionUsers.put(sessionId, userId);

            presenceService.connected(userId, sessionId);
        }
    }

    @EventListener
    public void disconnected(SessionDisconnectEvent event) {
        var userId = sessionUsers.remove(event.getSessionId());

        if (userId != null) {
            presenceService.disconnected(userId, event.getSessionId());
        }
    }
}
