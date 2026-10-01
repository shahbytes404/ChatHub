package com.shahbytes.chathub.messaging;

import com.shahbytes.chathub.api.dto.event.MessageCreatedEvent;
import com.shahbytes.chathub.api.dto.event.RealtimeEvent;
import com.shahbytes.chathub.domain.ProcessedEvent;
import com.shahbytes.chathub.repository.ProcessedEventRepository;
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

    private final RedisRealtimePublisher redisRealtimePublisher;

    private final String streamKey;
    private final String consumerGroup;
    private final String consumerName;


    public MessageDeliveryWorker(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            ProcessedEventRepository processedEventRepository,
            RedisRealtimePublisher redisRealtimePublisher,
            @Value("${chathub.messaging.stream-key}")
            String streamKey,
            @Value("${chathub.messaging.consumer-group}")
            String consumerGroup,
            @Value("${chathub.messaging.consumer-name}")
            String consumerName) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.processedEventRepository = processedEventRepository;
        this.redisRealtimePublisher = redisRealtimePublisher;
        this.streamKey = streamKey;
        this.consumerGroup = consumerGroup;
        this.consumerName = consumerName;
    }

    @Scheduled(
            fixedDelayString = "${chathub.messaging.delivery-poll-delay-ms}"
    )
    @Transactional
    public void consume() {
        ensureConsumerGroup();

        List<MapRecord<String, Object, Object>> records;

        try {
            records = streams().read(
                    Consumer.from(consumerGroup, consumerName),
                    StreamReadOptions.empty().count(100),
                    StreamOffset.create(
                            streamKey,
                            ReadOffset.lastConsumed()
                    )
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
            var ownPending =
                    streams().read(
                            Consumer.from(consumerGroup, consumerName),
                            StreamReadOptions.empty().count(100),
                            StreamOffset.create(
                                    streamKey,
                                    ReadOffset.from("0")
                            )
                    );

            if (ownPending != null) {
                ownPending.forEach(this::process);
            }

            var pending = streams().pending(
                    streamKey, consumerGroup, Range.unbounded(), 100);

            var staleIds = pending.stream()
                    .filter(this::isStaleFromAnotherConsumer)
                    .map(PendingMessage::getId)
                    .toArray(RecordId[]::new);

            if (staleIds.length > 0) {
                streams().claim(
                        streamKey,
                        consumerGroup,
                        consumerName,
                        Duration.ofMinutes(30),
                        staleIds
                ).forEach(this::process);
            }
        } catch (RuntimeException ignored) {
            ignored.printStackTrace();
        }
    }

    private boolean isStaleFromAnotherConsumer(PendingMessage pendingMessage) {
        return !consumerName.equals(pendingMessage.getConsumerName())
                &&
                pendingMessage.getElapsedTimeSinceLastDelivery()
                        .compareTo(
                                Duration.ofSeconds(30)
                        ) >= 0;
    }

    private void process(MapRecord<String, Object, Object> record) {
        var eventIdRaw = String.valueOf(
                record.getValue().get("eventId")
        );

        var payloadJson = String.valueOf(
                record.getValue().get("payloadJson")
        );

        try {
            var eventId = UUID.fromString(eventIdRaw);

            if (processedEventRepository.existsById(eventId)) {
                acknowledge(record);
                return;
            }

            var event = objectMapper.readValue(
                    payloadJson,
                    MessageCreatedEvent.class
            );

            for (var recipientId : event.recipientIds()) {
                var realtimeEvent = new RealtimeEvent(
                        "MESSAGE_CREATED",
                        recipientId,
                        event.message().conversationId(),
                        event.message().senderId(),
                        event.message().id(),
                        event.message(),
                        Instant.now()
                );

                redisRealtimePublisher.publish(realtimeEvent);
            }

            processedEventRepository.save(
                    new ProcessedEvent(
                            eventId,
                            Instant.now()
                    )
            );

            acknowledge(record);
        } catch (Exception e) {
            // ignored
            e.printStackTrace();
        }
    }

    private void acknowledge(MapRecord<String, Object, Object> record) {
        streams().acknowledge(
                streamKey,
                consumerGroup,
                record.getId()
        );
    }

    private void ensureConsumerGroup() {
        try {
            streams().createGroup(
                    streamKey,
                    ReadOffset.from("0-0"),
                    consumerGroup
            );
        } catch (RuntimeException ignored) {
            // ignored
        }
    }

    private StreamOperations<String, Object, Object> streams() {
        return redisTemplate.opsForStream();
    }
}
