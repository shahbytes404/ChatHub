package com.shabytes.backend.api;

import com.shabytes.backend.api.dto.TypingEvent;
import com.shabytes.backend.security.ChatPrincipal;
import com.shabytes.backend.service.PresenceService;
import com.shabytes.backend.service.TypingService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class ChatWebSocketController {
    private final PresenceService presenceService;
    private final TypingService typingService;

    public ChatWebSocketController(PresenceService presenceService, TypingService typingService) {
        this.presenceService = presenceService;
        this.typingService = typingService;
    }

    @MessageMapping("/presence/heartbeat")
    public void heartbeat(Principal principal, StompHeaderAccessor accessor) {
        presenceService.heartbeat(userId(principal), accessor.getSessionId());
    }

    @MessageMapping("/conversations/{conversationId}/typing")
    public void typing(
            Principal principal,
            @DestinationVariable UUID conversationId,
            @Valid TypingEvent event
    ) {
        typingService.update(userId(principal), conversationId, event.typing());
    }

    private UUID userId(Principal principal) {
        return ((ChatPrincipal) principal).userId();
    }
}
