package com.shahbytes.chathub.repository;

import com.shahbytes.chathub.domain.MessageReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageReceiptRepository extends JpaRepository<MessageReceipt, UUID> {
    List<MessageReceipt> findAllByMessageId(UUID messageId);

    Optional<MessageReceipt> findByMessageIdAndUserId(UUID messageId, UUID userId);
}
