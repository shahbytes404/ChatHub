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
    </aside>
  );
}
