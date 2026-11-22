import type { Conversation, SidebarMode } from "../../core/models";

interface ConversationListProps {
  conversations: Conversation[];
  allConversations: Conversation[];
  sidebarMode: SidebarMode;
  selectedId: string | null;
  onlineCount: number;
  totalCount: number;
  currentUserId?: string;
  currentUserName: string;
}

export function ConversationList({
  conversations,
  allConversations,
  sidebarMode,
  selectedId,
  onlineCount,
  totalCount,
  currentUserId,
  currentUserName,
}: ConversationListProps) {
  const getConversationTitle = (conversation: Conversation) => {
    if (conversation.type === "GROUP" && conversation.title) {
      return conversation.title;
    }

    // for groups, use "," otherwise one for direct
    return conversation.members
      .filter((member) => member.userId !== currentUserId)
      .map((member) => member.displayName)
      .join(", ");
  };

  return (
    <aside>
      <header>
        <div>
          <h2>{currentUserName}</h2>
          <p>
            {sidebarMode === "search"
              ? "Search across all messages"
              : `${onlineCount} online of ${totalCount} contacts`}
          </p>
        </div>
      </header>

      <div>
        {conversations.length ? (
          conversations.map((conversation) => {
            const title = getConversationTitle(conversation);

            return (
              <div key={conversation.id} tabIndex={0}>
                <span className="avatar">
                  {title.slice(0, 1).toUpperCase() || "C"}
                </span>
                <span>
                  <span>
                    <strong>{title}</strong>
                  </span>
                </span>
              </div>
            );
          })
        ) : (
          <p>No converstaions yet.</p>
        )}
      </div>
    </aside>
  );
}
