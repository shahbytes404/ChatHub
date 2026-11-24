import { MessageCirclePlus, Trash2 } from "lucide-react";
import type {
  Conversation,
  DeleteConversationAction,
  SidebarMode,
} from "../../core/models";
import { getConversationTitle } from "../../utils/conversation-utils";
import { formatShortTime } from "../../utils/utils";
import { useState } from "react";

import styles from "./ConversationList.module.css";

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
  unreadByConversation: Record<string, number>;
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
  unreadByConversation,
}: ConversationListProps) {
  const [deleteMenuConversationId, setDeleteMenuConversationId] = useState<
    string | null
  >(null);

  const canDeletePermanently = (conversation: Conversation) => {
    const role = conversation.members.find(
      (member) => member.userId === currentUserId,
    )?.role;
    return conversation.type === "GROUP" && role === "OWNER";
  };

  return (
    <aside className={styles.sidebar}>
      <header className={styles.sidebarHeader}>
        <div>
          <h2 className={styles.sidebarTitle}>{currentUserName}</h2>
          <p className="muted">
            {sidebarMode === "search"
              ? "Search across all messages"
              : `${onlineCount} online of ${totalCount} contacts`}
          </p>
        </div>

        <div className={styles.sidebarActions}>
          {
            <button
              className="icon-button accent"
              type="button"
              title="New chat"
              onClick={onNewConversation}>
              <MessageCirclePlus size={20} />
            </button>
          }
        </div>
      </header>

      <div className={styles.conversationList}>
        {conversations.length ? (
          conversations.map((conversation) => {
            const title = getConversationTitle(conversation, currentUserId);

            return (
              <div
                key={conversation.id}
                className={`${styles.conversationRow} ${conversation.id === selectedId ? styles.conversationRowActive : ""}`}
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
                <span className={styles.conversationCopy}>
                  <span className={styles.conversationTopline}>
                    <strong className={styles.conversationName}>{title}</strong>
                    <span className={styles.conversationTime}>
                      {formatShortTime(conversation.createdAt)}
                    </span>
                  </span>
                  <small className={`${styles.conversationPreview} muted`}>
                    {conversation.latestMessagePreview}
                  </small>
                </span>
                <span className={styles.rowActions}>
                  {(unreadByConversation[conversation.id] ?? 0) > 0 ? (
                    <span className="unread-badge">
                      {unreadByConversation[conversation.id]}
                    </span>
                  ) : null}
                  <button
                    className="icon-button danger ghost"
                    type="button"
                    title="Delete conversation"
                    onClick={(event) => {
                      event.stopPropagation();
                      if (!canDeletePermanently(conversation)) {
                        onDeleteConversation({
                          conversation,
                          permanent: false,
                        });
                        return;
                      }
                      setDeleteMenuConversationId((current) =>
                        current === conversation.id ? null : conversation.id,
                      );
                    }}>
                    <Trash2 size={17} />
                  </button>

                  {deleteMenuConversationId === conversation.id &&
                  canDeletePermanently(conversation) ? (
                    <div
                      className={styles.deleteMenu}
                      onClick={(event) => event.stopPropagation()}>
                      <button
                        type="button"
                        className={styles.deleteMenuItem}
                        onClick={() => {
                          setDeleteMenuConversationId(null);
                          onDeleteConversation({
                            conversation,
                            permanent: false,
                          });
                        }}>
                        Delete conversation
                      </button>
                      <button
                        type="button"
                        className={`${styles.deleteMenuItem} ${styles.deleteMenuItemDanger}`}
                        onClick={() => {
                          setDeleteMenuConversationId(null);
                          onDeleteConversation({
                            conversation,
                            permanent: true,
                          });
                        }}>
                        Delete group
                      </button>
                    </div>
                  ) : null}
                </span>
              </div>
            );
          })
        ) : (
          <p className="empty-state">No converstaions yet.</p>
        )}
      </div>
    </aside>
  );
}
