import type { Conversation } from "../core/models";

export function mergeLocalReadProgress(
  converstaion: Conversation,
  existingConv: Conversation | undefined,
  currentUserId: string | undefined,
): Conversation {
  if (!currentUserId || !existingConv) {
    return converstaion;
  }

  const existingMember = existingConv.members.find(
    (member) => member.userId === currentUserId,
  );

  if (!existingMember) {
    return converstaion;
  }

  return {
    ...converstaion,
    members: converstaion.members.map((member) =>
      member.userId === currentUserId
        ? {
            ...member,
            lastReadSequence: Math.max(
              member.lastReadSequence,
              existingMember.lastReadSequence,
            ),
          }
        : member,
    ),
  };
}

export const getConversationTitle = (
  conversation: Conversation,
  currentUserId: string | undefined,
) => {
  if (conversation.type === "GROUP" && conversation.title) {
    return conversation.title;
  }

  // for groups, use "," otherwise one for direct
  return conversation.members
    .filter((member) => member.userId !== currentUserId)
    .map((member) => member.displayName)
    .join(", ");
};
