package com.shahbytes.chathub.messaging;

import com.shahbytes.chathub.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;
    private final StringRedisTemplate redisTemplate;
    private final String streamKey;


    public OutboxPublisher(
            OutboxRepository outboxRepository,
            StringRedisTemplate redisTemplate,
            @Value("${chathub.messaging.stream-key}")
            String streamKey) {
        this.outboxRepository = outboxRepository;
        this.redisTemplate = redisTemplate;
        this.streamKey = streamKey;
    }

    @Scheduled(
            fixedDelayString = "${chathub.messaging.outbox-poll-delay-ms}"
    )
    @Transactional
    public void publishPendingEvents() {
        var events = outboxRepository.findReadyForPublish(Instant.now(), 100);

        for (var event : events) {
            try {
                redisTemplate.
                        opsForStream()
                        .add(
                                streamKey,
                                Map.of(
                                        "eventId",
                                        event.getId().toString(),

                                        "eventType",
                                        event.getEventType(),

                                        "payloadJson",
                                        event.getPayloadJson()
                                )
                        );
                event.markPublished();
            } catch (RuntimeException publishFailure) {
                event.scheduleRetry();
            }
        }
    }
}
