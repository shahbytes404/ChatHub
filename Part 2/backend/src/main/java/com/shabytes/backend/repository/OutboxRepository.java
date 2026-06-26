package com.shabytes.backend.repository;

import com.shabytes.backend.domain.OutboxEvent;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    // backend-1 lock rows 1-100
    // backend-2 lock rows 101-200
    @Query(value = """
                SELECT * from outbox_events
                    WHERE status = 'PENDING'
                        AND available_at <= :now
                            order by created_at
                                LIMIT :batchSize
                                    FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> findReadyForPublish(@Param("now") Instant now, @Param("batchSize") int batchSize);
}
