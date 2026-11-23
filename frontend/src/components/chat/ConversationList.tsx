import { MessageCirclePlus } from "lucide-react";
import type {
  Conversation,
  DeleteConversationAction,
  SidebarMode,
} from "../../core/models";
import { getConversationTitle } from "../../utils/conversation-utils";

interface ConversationListProps {
  conversations: Conversation[];
  sidebarMode: SidebarMode;
  selectedId: string | null;
  onlineCount: number;
  totalCount: number;
  currentUserId?: string;
  currentUserName: string;
  onSelectConversation: (conversation: Conversation) => void;
  onNewConversation: () => void;
  onDeleteConversation: (action: DeleteConversationAction) => void;
}

export function ConversationList({
  conversations,
  sidebarMode,
  selectedId,
  onlineCount,
  totalCount,
  currentUserId,
  currentUserName,
  onSelectConversation,
  onNewConversation,
  onDeleteConversation,
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

        <div>
          {
            <button type="button" title="New chat" onClick={onNewConversation}>
              <MessageCirclePlus size={20} />
            </button>
          }
        </div>
      </header>

      <div>
        {conversations.length ? (
          conversations.map((conversation) => {
            const title = getConversationTitle(conversation, currentUserId);

            return (
              <div
                key={conversation.id}
                tabIndex={0}
                onClick={() => onSelectConversation(conversation)}
                onKeyDown={(event) => {
                  if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    onSelectConversation(conversation);
                  }
                }}>
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
