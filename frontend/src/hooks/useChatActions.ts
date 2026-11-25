import { chatApi } from "../core/chat-api";
import { CONVERSATION_TYPES, MEMBER_ROLES } from "../core/constants";
import type {
  ChatMessage,
  ContactSelection,
  Conversation,
  PickerMode,
} from "../core/models";

interface UseChatActionsOptions {
  currentUserId: string | undefined;
  selectedConversation: Conversation | null;
  appendMessage: (message: ChatMessage) => boolean;
  removeConversation: (conversationId: string) => void;
  removeConversationMessages: (conversationId: string) => ChatMessage[];
  selectConversation: (conversation: Conversation) => void;
  conversations: Conversation[];
  upsertConversation: (conversation: Conversation) => void;
  onError: (message: string) => void;
}

export function useChatActions({
  currentUserId,
  selectedConversation,
  conversations,
  upsertConversation,
  selectConversation,
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

  const addUserToConversation = async (
    conversation: Conversation,
    selection: ContactSelection,
  ) => {
    if (conversation.type === CONVERSATION_TYPES.DIRECT) {
      // Start a separate group with the original recipient and the picked user.
      const otherMemberIds = conversation.members
        .map((member) => member.userId)
        .filter((userId) => userId !== currentUserId);
      // Pass the other participants once each. The server includes the creator.
      const created = await chatApi.createConversation({
        type: CONVERSATION_TYPES.GROUP,
        memberIds: [...new Set([...otherMemberIds, selection.user.id])],
      });

      upsertConversation(created);
      selectConversation(created);
      return;
    }

    // An existing group keeps its conversation ID and stays open after the update.
    const updated = await chatApi.addMember(
      conversation.id,
      selection.user.id,
      selection.role ?? MEMBER_ROLES.MEMBER,
    );
    upsertConversation(updated);
  };

  const openDirectConversation = async (userId: string) => {
    // It sorts the IDs before comparing them, so [bob, alice] matches [alice, bob].
    // Sort the IDs so a direct chat matches regardless of member order.
    const expectedMembers = [currentUserId, userId].sort().join("|");
    const existing = conversations.find(
      (conversation) =>
        conversation.type === CONVERSATION_TYPES.DIRECT &&
        conversation.members
          .map((member) => member.userId)
          .sort()
          .join("|") === expectedMembers,
    );

    // Reopen the existing chat so its message history stays with the conversation.
    if (existing) {
      selectConversation(existing);
      return;
    }

    const created = await chatApi.createConversation({
      type: CONVERSATION_TYPES.DIRECT,
      memberIds: [userId],
    });
    // Add the new chat to local state so the workspace can resolve its selected ID.
    upsertConversation(created);
    selectConversation(created);
  };

  const handlePickedUser = async (
    selection: ContactSelection,
    pickerMode: PickerMode,
  ) => {
    try {
      if (pickerMode === "add" && selectedConversation) {
        await addUserToConversation(selectedConversation, selection);
        return;
      }

      // Add mode also falls back here if there is no selected conversation.
      await openDirectConversation(selection.user.id);
    } catch (requestError) {
      const apiError = requestError as {
        error?: {
          message?: string;
        };
      };

      onError(apiError.error?.message ?? "Could not complete that action.");
    }
  };

  return {
    sendMessage,
    deleteConversation,
    handlePickedUser,
  };
}
