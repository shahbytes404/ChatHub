import { Users } from "lucide-react";
import type { ChatMessage, Conversation } from "../../core/models";
import { formatShortTime, randomColor } from "../../utils/utils";
import { receiptGlyph, receiptStateForMessage } from "../../utils/message-utils";
import styles from "./MessageFeed.module.css";

interface MessageFeedProps {
  conversation: Conversation;
  messages: ChatMessage[];
  currentUserId?: string;
}

export function MessageFeed({
  conversation,
  messages,
  currentUserId,
}: MessageFeedProps) {
  return (
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
  );
}
