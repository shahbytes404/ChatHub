package com.shahbytes.chathub.service;

import com.shahbytes.chathub.domain.Message;
import com.shahbytes.chathub.domain.MessageReceipt;
import com.shahbytes.chathub.domain.type.ReceiptState;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
public class MessageReceiptStateService {

    public Optional<ReceiptState> resolveForSender(
            Message message,
            UUID currentUserId,
            Collection<MessageReceipt> receipts,
            Collection<UUID> currentMemberIds
    ) {
        if (!message.getSenderId().equals(currentUserId)) {
            return Optional.empty();
        }

        var activeReceipts = receipts.stream()
                .filter(receipt -> currentMemberIds.contains(receipt.getUserId()))
                .toList();

        if (activeReceipts.isEmpty()) {
            return Optional.of(ReceiptState.SENT);
        }

        boolean allDelivered = activeReceipts.stream()
                .allMatch(receipt -> receipt.getDeliveredAt() != null);

        if (!allDelivered) {
            return Optional.of(ReceiptState.SENT);
        }

        boolean allRead = activeReceipts.stream()
                .allMatch(receipt -> receipt.getReadAt() != null);

        if (!allRead) {
            return Optional.of(ReceiptState.DELIVERED);
        }

        return Optional.of(ReceiptState.READ);
    }
}
