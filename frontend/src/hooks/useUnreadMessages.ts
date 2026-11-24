import { useState } from "react";
import type { ChatMessage, Conversation } from "../core/models";
import { calculateUnreadCount } from "../utils/message-utils";

interface UseUnreadMessagesOptions {
  currentUserId: string | undefined;

  getConversation: (conversationId: string) => Conversation | undefined;
}

export function useUnreadMessages({
  currentUserId,
  getConversation,
}: UseUnreadMessagesOptions) {
  const [unreadByConversation, setUnreadByConversation] = useState<
    Record<string, number>
  >({});

  const clearUnread = (conversationId: string) => {
    setUnreadByConversation((state) => ({
      ...state,
      [conversationId]: 0,
    }));
  };

  const refreshUnread = (
    conversationId: string,
    messages: ChatMessage[] = [],
  ) => {
    const conversation = getConversation(conversationId);

    const unread = calculateUnreadCount(conversation, messages, currentUserId);

    setUnreadByConversation((state) => ({
      ...state,
      [conversationId]: unread,
    }));
  };

  const clearUnreadMessages = () => {
    setUnreadByConversation({});
  };

  return {
    unreadByConversation,
    setUnreadByConversation,
    clearUnread,
    refreshUnread,
    clearUnreadMessages,
  };
}
