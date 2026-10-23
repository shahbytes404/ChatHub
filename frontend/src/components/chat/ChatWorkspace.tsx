import { useState } from "react";
import type { AuthResponse, SidebarMode, ThemeMode } from "../../core/models";
import { LogOut, MessageCircle, Moon, Search, Sun } from "lucide-react";
import { authStore } from "../../core/auth-store";
import styles from "./ChatWorkspace.module.css";
import { ConversationList } from "./ConversationList";
import { ChatWindow } from "./ChatWindow";

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
          onClick={() => {}}>
          <MessageCircle size={18} />
        </button>
        <button
          className={`${styles.railIcon} ${sidebarMode === "search" ? styles.railIconActive : ""}`}
          type="button"
          title="Search chats"
          onClick={() => {}}>
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
        <ConversationList />
        <ChatWindow />
      </section>
    </main>
  );
}
