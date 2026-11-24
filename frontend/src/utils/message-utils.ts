import type { ReceiptState } from "../core/constants";
import type { ChatMessage, Conversation } from "../core/models";

export const receiptGlyph = (state: ReceiptState | null | undefined) => {
  if (state === "DELIVERED" || state === "READ") {
    return "✓✓";
  }

  return "✓";
};

export const receiptStateForMessage = (
  message: ChatMessage,
  currentUserId: string | undefined,
): ReceiptState | null => {
  if (message.senderId !== currentUserId) {
    return null;
  }

  return message.receiptState ?? "SENT";
};
