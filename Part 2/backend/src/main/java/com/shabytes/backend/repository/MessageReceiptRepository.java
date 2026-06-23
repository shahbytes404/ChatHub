package com.shabytes.backend.repository;

import com.shabytes.backend.domain.MessageReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MessageReceiptRepository extends JpaRepository<MessageReceipt, UUID> {
}
