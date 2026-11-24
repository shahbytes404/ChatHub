import { useState } from "react";
import type { ChatMessage, Conversation } from "../core/models";
import { chatApi } from "../core/chat-api";

interface UseMessagesOptions {
  getConversation: (conversationId: string) => Conversation | undefined;
  onUpdateConversationPreview: (
    conversationId: string,
    preview: string,
  ) => void;

  currentUserId: string | undefined;
  selectedConversationId: string | null;

  onError: (message: string) => void;
}

export function useMessages({
  getConversation,
  onUpdateConversationPreview,
  currentUserId,
  selectedConversationId,
  onError,
}: UseMessagesOptions) {
  const [messagesByConversation, setMessagesByConversation] = useState<
    Record<string, ChatMessage[]>
  >({});

  const appendMessage = (message: ChatMessage) => {
    const conversation = getConversation(message.conversationId);

    if (!conversation) {
      return false;
    }

    const myself = conversation.members.find(
      (member) => member.userId === currentUserId,
    );

    const hiddenAfterSequence = myself?.hiddenAfterSequence ?? null;

    if (
      hiddenAfterSequence != null &&
      message.sequenceNumber <= hiddenAfterSequence
    ) {
      return false;
    }

    const existingMessages =
      messagesByConversation[message.conversationId] ?? [];

    const alreadyExists = existingMessages.some(
      (item) => item.id === message.id,
    );

    if (alreadyExists) {
      return false;
    }

    setMessagesByConversation((state) => {
      const existing = state[message.conversationId] ?? [];

      const visibleExisting =
        hiddenAfterSequence == null
          ? existing
          : existing.filter(
              (item) => item.sequenceNumber > hiddenAfterSequence,
            );

      if (visibleExisting.some((item) => item.id === message.id)) {
        return state;
      }

      return {
        ...state,
        [message.conversationId]: [...visibleExisting, message].sort(
          (left, right) => left.sequenceNumber - right.sequenceNumber,
        ),
      };
    });

    onUpdateConversationPreview(message.conversationId, message.content);

    return true;
  };

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

  const removeConversationMessages = (conversationId: string) => {
    const messages = messagesByConversation[conversationId] ?? [];

    setMessagesByConversation((state) => {
      const next = { ...state };
      delete next[conversationId];
      return next;
    });

    return messages;
  };

  const selectedMessages = selectedConversationId
    ? (messagesByConversation[selectedConversationId] ?? [])
    : [];

  return {
    messagesByConversation,
    selectedMessages,
    loadMessages,
    appendMessage,
    removeConversationMessages,
  };
}
