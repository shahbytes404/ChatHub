import { Send, Users } from "lucide-react";
import type { ChatMessage, Conversation } from "../../core/models";
import { useMemo, useState } from "react";
import { getConversationTitle } from "../../utils/conversation-utils";
import { formatShortTime, randomColor } from "../../utils/utils";
import {
  receiptGlyph,
  receiptStateForMessage,
} from "../../utils/message-utils";
import type { SubmitEvent } from "react";
import styles from "./ChatWindow.module.css";

interface ChatWindowProps {
  conversation: Conversation | null;
  messages: ChatMessage[];
  currentUserId?: string;
  onSendMessage: (content: string) => void;
}

export function ChatWindow({
  conversation,
  messages,
  currentUserId,
  onSendMessage,
}: ChatWindowProps) {
  const [draft, setDraft] = useState("");

  const title = useMemo(() => {
    if (!conversation) {
      return "";
    }
    return getConversationTitle(conversation, currentUserId);
  }, [conversation, currentUserId]);

  const submit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    const content = draft.trim();
    if (!content) {
      return;
    }
    onSendMessage(content);
    setDraft("");
  };

  if (!conversation) {
    return (
      <section className={styles.chatEmpty}>
        <Users size={42} />
        <h2>Select a conversation</h2>
        <p className="muted">Create a chat or open one from the list.</p>
      </section>
    );
  }

  return (
    <section className={styles.chatPane}>
      <header className={styles.chatHeader}>
        <div className={styles.chatTitle}>
          <span className="avatar large">
            {title.slice(0, 1).toUpperCase() || "C"}
          </span>
          <div className={styles.chatTitleCopy}>
            <h2 className={styles.chatName}>{title}</h2>
          </div>
        </div>
      </header>

      <div className={styles.messageFeed}>
        {messages.length ? (
          messages.map((message) =>
            message.type === "SYSTEM" ? (
              <article key={message.id} className={styles.systemBubble}>
                <span>{message.content}</span>
              </article>
            ) : (
              <article
                key={message.id}
                className={`${styles.messageBubble} ${message.senderId === currentUserId ? styles.messageBubbleMine : ""}`}>
                {conversation.type === "GROUP" &&
                message.senderId !== currentUserId ? (
                  <div
                    className={styles.senderName}
                    style={{ color: randomColor(message.senderId) }}>
                    {conversation.members.find(
                      (member) => member.userId === message.senderId,
                    )?.displayName ?? "Unknown"}
                  </div>
                ) : null}
                <p className={styles.messageText}>{message.content}</p>
                <time className={styles.messageMeta}>
                  {formatShortTime(message.createdAt)}
                  {message.senderId === currentUserId ? (
                    <span
                      className={`${styles.receipt} ${
                        receiptStateForMessage(message, currentUserId) ===
                        "READ"
                          ? styles.receiptRead
                          : ""
                      }`}>
                      {receiptGlyph(
                        receiptStateForMessage(message, currentUserId),
                      )}
                    </span>
                  ) : null}
                </time>
              </article>
            ),
          )
        ) : (
          <div className={styles.welcomePanel}>
            <Users size={32} />
            <h3>Start the conversation</h3>
            <p className="muted">
              Messages, typing indicators, and group changes will update here.
            </p>
          </div>
        )}
      </div>

      <form className={styles.composer} onSubmit={submit}>
        <textarea
          className={styles.composerInput}
          rows={1}
          placeholder="Message"
          value={draft}
          onChange={(event) => {
            const value = event.target.value;
            setDraft(value);
          }}
          onKeyDown={(event) => {
            if (event.key === "Enter" && !event.shiftKey) {
              event.preventDefault();
              if (draft.trim()) {
                onSendMessage(draft.trim());
                setDraft("");
              }
            }
          }}
        />
        <button
          className={styles.sendButton}
          type="submit"
          title="Send message"
          disabled={!draft.trim()}>
          <Send size={20} />
        </button>
      </form>
    </section>
  );
}
