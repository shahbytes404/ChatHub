import { useState } from "react";
import type { ChatMessage, Conversation } from "../core/models";
import { chatApi } from "../core/chat-api";

interface UseMessagesOptions {
  getConversation: (conversationId: string) => Conversation | undefined;

  currentUserId: string | undefined;
  selectedConversationId: string | null;

  onError: (message: string) => void;
}

export function useMessages({
  getConversation,
  currentUserId,
  selectedConversationId,
  onError,
}: UseMessagesOptions) {
  const [messagesByConversation, setMessagesByConversation] = useState<
    Record<string, ChatMessage[]>
  >({});

  const loadMessages = async (conversationId: string) => {
    try {
      const page = await chatApi.messages(conversationId);

      const conversation = getConversation(conversationId);

      if (!conversation) {
        return;
      }

      const myself = conversation.members.find(
        (member) => member.userId === currentUserId,
      );

      const hiddenAfterSequence = myself?.hiddenAfterSequence ?? null;

      const visibleMessages = page.messages.filter(
        (message) =>
          hiddenAfterSequence == null ||
          message.sequenceNumber > hiddenAfterSequence,
      );

      setMessagesByConversation((state) => ({
        ...state,
        [conversationId]: visibleMessages,
      }));
    } catch (requestError) {
      const errorWithStatus = requestError as {
        status?: number;
        error?: {
          message?: string;
        };
      };
      onError(errorWithStatus.error?.message ?? "Could not load messages.");
    }
  };

  const selectedMessages = selectedConversationId
    ? (messagesByConversation[selectedConversationId] ?? [])
    : [];

  return { messagesByConversation, selectedMessages, loadMessages };
}
