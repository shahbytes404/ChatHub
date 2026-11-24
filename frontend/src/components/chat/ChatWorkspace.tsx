import { useEffect, useMemo, useState } from "react";
import type { AuthResponse, SidebarMode, ThemeMode } from "../../core/models";
import { LogOut, MessageCircle, Moon, Search, Sun } from "lucide-react";
import { authStore } from "../../core/auth-store";
import styles from "./ChatWorkspace.module.css";
import { ConversationList } from "./ConversationList";
import { ChatWindow } from "./ChatWindow";
import { useConversations } from "../../hooks/useConversations";
import { useChatSelection } from "../../hooks/useChatSelection";
import { useMessages } from "../../hooks/useMessages";
import { useUnreadMessages } from "../../hooks/useUnreadMessages";
import { useChatActions } from "../../hooks/useChatActions";

interface ChatWorkspaceProps {
  session: AuthResponse;
  theme: ThemeMode;
  onToggleTheme: () => void;
}

export function ChatWorkspace({
  session,
  theme,
  onToggleTheme,
}: ChatWorkspaceProps) {
  const [sidebarMode, setSidebarMode] = useState<SidebarMode>("chats");
  const [error, setError] = useState("");

  const currentUserId = session.userId;
  const currentUserName = session.displayName ?? "You";

  const [selectedConversationId, setSelectedConversationId] = useState<
    string | null
  >(null);

  const totalCount = 10;

  const onlineCount = 5;

  const {
    conversations,
    getConversation,
    loadConversations,
    updateConversationPreview,
    upsertConversations,
    removeConversation,
    clearConversations,
  } = useConversations({ currentUserId, onError: setError });

  const {
    unreadByConversation,
    setUnreadByConversation,
    clearUnread,
    refreshUnread,
    clearUnreadMessages,
  } = useUnreadMessages({
    currentUserId,
    getConversation,
  });

  const {
    messagesByConversation,
    selectedMessages,
    loadMessages,
    appendMessage,
    removeConversationMessages,
  } = useMessages({
    getConversation,
    currentUserId,
    selectedConversationId,
    onUpdateConversationPreview: updateConversationPreview,
    onError: setError,
  });

  const { selectConversation } = useChatSelection({
    currentUserId,
    setSelectedConversationId,
    loadMessages,
  });

  const selectedConversation = useMemo(
    () =>
      conversations.find(
        (conversation) => conversation.id === selectedConversationId,
      ) ?? null,
    [conversations, selectedConversationId],
  );

  const { sendMessage, deleteConversation } = useChatActions({
    currentUserId,
    selectedConversation,
    appendMessage,
    removeConversation,
    removeConversationMessages,
    onError: setError,
  });

  // load conversations
  useEffect(() => {
    const token = session.accessToken ?? null;
    if (!token) {
      return;
    }

    void loadConversations();
  }, [loadConversations, session.accessToken]);

  return (
    <main className={styles.appShell}>
      <aside className={styles.rail}>
        <button className={styles.railAvatar} type="button" title="Profile">
          <span>{session.displayName.slice(0, 1).toUpperCase() || "U"}</span>
        </button>
        <button
          className={`${styles.railIcon} ${sidebarMode === "chats" ? styles.railIconActive : ""}`}
          type="button"
          title="Chats"
          onClick={() => {
            setSidebarMode("chats");
          }}>
          <MessageCircle size={18} />
        </button>
        <button
          className={`${styles.railIcon} ${sidebarMode === "search" ? styles.railIconActive : ""}`}
          type="button"
          title="Search chats"
          onClick={() => {
            setSidebarMode("search");
          }}>
          <Search size={18} />
        </button>
        <button
          className={styles.railIcon}
          type="button"
          title={
            theme === "dark" ? "Switch to light mode" : "Switch to dark mode"
          }
          onClick={onToggleTheme}>
          {theme === "dark" ? <Sun size={18} /> : <Moon size={18} />}
        </button>
        <button
          className={`${styles.railIcon} ${styles.railIconDanger}`}
          type="button"
          title="Sign out"
          onClick={() => authStore.signOut()}>
          <LogOut size={18} />
        </button>
      </aside>

      {error ? (
        <button
          className={styles.toast}
          type="button"
          onClick={() => setError("")}>
          {error}
        </button>
      ) : null}

      <section className={styles.workspace}>
        <ConversationList
          sidebarMode={sidebarMode}
          conversations={conversations}
          onlineCount={onlineCount}
          totalCount={totalCount}
          currentUserName={currentUserName}
          currentUserId={currentUserId}
          selectedId={selectedConversationId}
          onSelectConversation={(conversation) => {
            void selectConversation(conversation);
          }}
          onNewConversation={() => {}}
          onDeleteConversation={({ conversation, permanent }) => {
            void deleteConversation(conversation, permanent);
          }}
          unreadByConversation={unreadByConversation}
        />
        <ChatWindow
          conversation={selectedConversation}
          messages={selectedMessages}
          currentUserId={currentUserId}
          onSendMessage={(content) => {
            void sendMessage(content);
          }}
        />
      </section>
    </main>
  );
}
