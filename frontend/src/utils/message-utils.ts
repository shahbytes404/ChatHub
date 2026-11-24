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

export function calculateUnreadCount(
  conversation: Conversation | undefined,
  messages: ChatMessage[],
  currentUserId: string | undefined,
): number {
  if (!conversation || !currentUserId) {
    return 0;
  }

  const myself = conversation.members.find(
    (member) => member.userId === currentUserId,
  );

  if (!myself) {
    return 0;
  }

  return messages
    .filter((message) => message.senderId !== currentUserId)
    .filter((message) => message.sequenceNumber > myself.lastReadSequence)
    .length;
}
