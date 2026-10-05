package com.shahbytes.chathub.repository;

import com.shahbytes.chathub.domain.MessageReceipt;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageReceiptRepository extends JpaRepository<MessageReceipt, UUID> {
    List<MessageReceipt> findAllByMessageId(UUID messageId);

    Optional<MessageReceipt> findByMessageIdAndUserId(UUID messageId, UUID userId);

    List<MessageReceipt> findAllByMessageIdIn(Collection<UUID> messageIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select r from MessageReceipt r where r.messageId = :messageId
            """)
    List<MessageReceipt> findAllByMessageIdForUpdate(@Param("messageId") UUID messageId);
}
