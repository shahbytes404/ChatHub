import { useCallback, useState } from "react";
import type { Conversation } from "../core/models";
import { chatApi } from "../core/chat-api";
import { mergeLocalReadProgress } from "../utils/conversation-utils";

interface useConversationOptions {
  currentUserId: string | undefined;
  onError: (message: string) => void;
}

export function useConversations({
  currentUserId,
  onError,
}: useConversationOptions) {
  const [conversations, setConversations] = useState<Conversation[]>([]);

  const getConversation = (
    conversationId: string,
  ): Conversation | undefined => {
    return conversations.find(
      (conversation) => conversation.id === conversationId,
    );
  };

  const loadConversations = useCallback(async () => {
    if (!currentUserId) {
      return;
    }

    try {
      const loaded = await chatApi.conversations();

      setConversations((prevConv) =>
        loaded.map((conversation) => {
          const existingConv = prevConv.find(
            (item) => item.id === conversation.id,
          );

          return mergeLocalReadProgress(
            conversation,
            existingConv,
            currentUserId,
          );
        }),
      );

      return loaded;
    } catch (requestError) {
      const apiError = requestError as {
        error?: {
          message?: string;
        };
      };

      onError(apiError.error?.message ?? "Could not load conversations");

      return undefined;
    }
  }, [currentUserId, onError]);

  // used in chat realtime, and chat actions
  const upsertConversations = (conversation: Conversation) => {
    setConversations((prevConv) => {
      const existing = prevConv.find((item) => item.id === conversation.id);

      // while inserting, thatc onv should not mess up teh last read sequnce, if its already present
      const merged = mergeLocalReadProgress(
        conversation,
        existing,
        currentUserId,
      );

      // Insert
      if (!existing) {
        return [merged, ...prevConv];
      }

      // now update
      return prevConv.map((item) =>
        item.id === conversation.id ? merged : item,
      );
    });
  };

  const updateConversationPreview = (
    conversationId: string,
    preview: string,
  ) => {
    setConversations((items) =>
      items.map((conversation) =>
        conversation.id === conversationId
          ? {
              ...conversation,
              latestMessagePreview: preview,
            }
          : conversation,
      ),
    );
  };

  const removeConversation = (conversationId: string) => {
    setConversations((prevConv) =>
      prevConv.filter((conv) => conv.id !== conversationId),
    );
  };

  // during sign out
  const clearConversations = () => {
    setConversations([]);
  };

  return {
    conversations,
    getConversation,
    loadConversations,
    clearConversations,
    upsertConversations,
    removeConversation,
    updateConversationPreview,
  };
}
