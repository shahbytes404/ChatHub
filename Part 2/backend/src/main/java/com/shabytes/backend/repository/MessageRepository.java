package com.shabytes.backend.repository;

import com.shabytes.backend.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Optional<Message> findBySenderIdAndClientMessageId(UUID senderId, String clientMessageId);
}
