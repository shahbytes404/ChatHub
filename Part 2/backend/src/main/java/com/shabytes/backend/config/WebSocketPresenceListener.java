package com.shabytes.backend.config;

import com.shabytes.backend.security.ChatPrincipal;
import com.shabytes.backend.service.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketPresenceListener {
    private final PresenceService presenceService;

    // sessionUsers["ws-session-a"] = user-123
    private final ConcurrentHashMap<String, UUID> sessionUsers = new ConcurrentHashMap<>();


    public WebSocketPresenceListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void connected(SessionConnectedEvent event) {
        var accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() instanceof ChatPrincipal principal && accessor.getSessionId() != null) {
            sessionUsers.put(accessor.getSessionId(), principal.userId());
            presenceService.connected(principal.userId(), accessor.getSessionId());
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
