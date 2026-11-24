import { chatApi } from "../core/chat-api";
import type { ChatMessage, Conversation } from "../core/models";

interface UseChatActionsOptions {
  currentUserId: string | undefined;
  selectedConversation: Conversation | null;
  appendMessage: (message: ChatMessage) => boolean;
  removeConversation: (conversationId: string) => void;
  removeConversationMessages: (conversationId: string) => ChatMessage[];
  onError: (message: string) => void;
}

export function useChatActions({
  currentUserId,
  selectedConversation,
  appendMessage,
  removeConversation,
  removeConversationMessages,
  onError,
}: UseChatActionsOptions) {
  const deleteConversation = async (
    conversation: Conversation,
    permanent: boolean,
  ) => {
    try {
      await chatApi.deleteConversation(conversation.id, permanent);

      removeConversationMessages(conversation.id);
      removeConversation(conversation.id);
    } catch (requestError) {
      const apiError = requestError as {
        error?: {
          message?: string;
        };
      };

      onError(apiError.error?.message ?? "Could not delete the conversation.");
    }
  };

  const sendMessage = async (content: string) => {
    if (!selectedConversation) {
      return;
    }

    try {
      const message = await chatApi.sendMessage(
        selectedConversation.id,
        content,
      );

      appendMessage(message);
    } catch (requestError) {
      const apiError = requestError as {
        error?: {
          message?: string;
        };
      };

      onError(apiError.error?.message ?? "Could not send message.");
    }
  };

  return {
    sendMessage,
    deleteConversation,
  };
}
