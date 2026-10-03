package com.shahbytes.chathub.api;

import com.shahbytes.chathub.api.dto.event.RealtimeEvent;
import com.shahbytes.chathub.domain.type.EventType;
import com.shahbytes.chathub.messaging.RedisRealtimePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/test/realtime")
@RequiredArgsConstructor
public class RealtimeTestController {

    private final RedisRealtimePublisher publisher;

    @PostMapping("/{userId}")
    public void test(@PathVariable UUID userId) {
        var event = new RealtimeEvent(
                EventType.TEST,
                userId,
                null,
                null,
                null,
                "Hello from Redis",
                Instant.now()
        );
        publisher.publish(event);
    }
}
