package com.shahbytes.chathub.repository;

import com.shahbytes.chathub.domain.OutboxEvent;
import com.shahbytes.chathub.domain.type.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = """
                SELECT *
                    FROM outbox_events
                        where status = :status
                            AND available_at <= :now
                                ORDER BY created_at
                                    LIMIT :batchSize 
                                        FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> findReadyForPublish(
            @Param("status") String status,
            @Param("now") Instant now,
            @Param("batchSize") int batchSize
    );

    default List<OutboxEvent> findReadyForPublish(
            Instant now,
            int batchSize
    ) {
        return findReadyForPublish(OutboxStatus.PENDING.name(), now, batchSize);
    }
}
