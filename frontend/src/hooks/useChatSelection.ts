import type { Conversation } from "../core/models";

interface UseChatSelectionOptions {
  currentUserId: string | undefined;
  setSelectedConversationId: (conversationId: string | null) => void;
  loadMessages: (conversationId: string) => void | Promise<void>;
}

export function useChatSelection({
  currentUserId,
  setSelectedConversationId,
  loadMessages,
}: UseChatSelectionOptions) {
  const selectConversation = async (conversation: Conversation) => {
    setSelectedConversationId(conversation.id);

    void loadMessages(conversation.id);
  };

  return {
    selectConversation,
  };
}
