package com.shabytes.backend.messaging;

import com.shabytes.backend.api.dto.RealtimeEvent;
import com.shabytes.backend.repository.ProcessedEventRepository;
import com.shabytes.backend.service.NotificationService;
import com.shabytes.backend.service.PresenceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MessageDeliveryWorker {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;
    private final PresenceService presenceService;
    private final RedisRealtimePublisher realtimePublisher;
    private final String streamKey;
    private final String consumerGroup;
    private final String consumerName;
    private final NotificationService notificationService;

    public MessageDeliveryWorker(StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
                                 ProcessedEventRepository processedEventRepository,
                                 PresenceService presenceService, RedisRealtimePublisher realtimePublisher,
                                 @Value("${chathub.messaging.stream-key}") String streamKey,
                                 @Value("${chathub.messaging.consumer-group}") String consumerGroup,
                                 @Value("${chathub.messaging.consumer-name}") String consumerName, NotificationService notificationService
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.processedEventRepository = processedEventRepository;
        this.presenceService = presenceService;
        this.realtimePublisher = realtimePublisher;
        this.streamKey = streamKey;
        this.consumerGroup = consumerGroup;
        this.consumerName = consumerName;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelayString = "${chathub.messaging.delivery-poll-delay-ms}")
    @Transactional
    public void consume() {
        ensureConsumerGroup();
        List<MapRecord<String, Object, Object>> records;
        try {
            records = streams().read(
                    Consumer.from(consumerGroup, consumerName),
                    StreamReadOptions.empty().count(100),
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed())
            );
        } catch (RuntimeException redisUnavailable) {
            return;
        }

        if (records == null) {
            return;
        }

        records.forEach(this::process);
    }

    @Scheduled(fixedDelay = 5_000)
    @Transactional
    public void recoverPending() {
        ensureConsumerGroup();
        try {
            var ownPending = streams().read(
                    Consumer.from(consumerGroup, consumerName),
                    StreamReadOptions.empty().count(100),
                    StreamOffset.create(streamKey, ReadOffset.from("0"))
            );

            if (ownPending != null) {
                ownPending.forEach(this::process);
            }

            var pending = streams().pending(
                    streamKey,
                    consumerGroup,
                    Range.unbounded(),
                    100
            );
            var staleIds = pending.stream()
                    .filter(this::isStaleFromAnotherConsumer)
                    .map(PendingMessage::getId)
                    .toArray(RecordId[]::new);

            if (staleIds.length > 0) {
                streams().claim(
                        streamKey,
                        consumerGroup,
                        consumerName,
                        Duration.ofSeconds(30),
                        staleIds
                ).forEach(this::process);
            }
        } catch (RuntimeException exception) {

        }
    }

    private void process(MapRecord<String, Object, Object> record) {
        var eventIdRaw = String.valueOf(record.getValue().get("eventId"));
        var payloadJson = String.valueOf(record.getValue().get("payloadJson"));

        try {
            var eventId = UUID.fromString(eventIdRaw);
            if (processedEventRepository.existsById(eventId)) {
                acknowledge(record);
                return;
            }

            var event = objectMapper.readValue(payloadJson, MessageCreatedEvent.class);
            for (var recipientId : event.recipientIds()) {
                if (presenceService.isOnline(recipientId)) {
                    realtimePublisher.publish((new RealtimeEvent(
                            "MESSAGE_CREATED",
                            recipientId,
                            event.message().conversationId(),
                            event.message().senderId(),
                            event.message().id(),
                            event.message(),
                            Instant.now()
                    )));
                } else {
                    notificationService.notifyOfflineUser(recipientId, event.message());
                }
            }
        } catch (Exception processingFailure) {
            processingFailure.printStackTrace();
        }
    }

    private void acknowledge(MapRecord<String, Object, Object> record) {
        streams().acknowledge(streamKey, consumerGroup, record.getId());
    }

    private boolean isStaleFromAnotherConsumer(PendingMessage pendingMessage) {
        return !consumerName.equals(pendingMessage.getConsumerName())
                && pendingMessage.getElapsedTimeSinceLastDelivery().compareTo(Duration.ofSeconds(30)) >= 0;
    }

    public void ensureConsumerGroup() {
        try {
            streams().createGroup(streamKey, ReadOffset.from("0-0"), consumerGroup);
        } catch (RuntimeException alreadyExistsOrStreamMissing) {

        }
    }

    private StreamOperations<String, Object, Object> streams() {
        return redisTemplate.opsForStream();
    }
}
