import { Users } from "lucide-react";
import type { ChatMessage, Conversation } from "../../core/models";
import { useMemo } from "react";
import { getConversationTitle } from "../../utils/conversation-utils";
import { formatShortTime } from "../../utils/utils";
import {
  receiptGlyph,
  receiptStateForMessage,
} from "../../utils/message-utils";

interface ChatWindowProps {
  conversation: Conversation | null;
  messages: ChatMessage[];
  currentUserId?: string;
}

export function ChatWindow({
  conversation,
  messages,
  currentUserId,
}: ChatWindowProps) {
  const title = useMemo(() => {
    if (!conversation) {
      return "";
    }
    return getConversationTitle(conversation, currentUserId);
  }, [conversation, currentUserId]);

  if (!conversation) {
    return (
      <section>
        <Users size={42} />
        <h2>Select a conversation</h2>
        <p className="muted">Create a chat or open one from the list.</p>
      </section>
    );
  }

  return (
    <section>
      <header>
        <div>
          <span className="avatar large">
            {title.slice(0, 1).toUpperCase() || "C"}
          </span>
          <div>
            <h2>{title}</h2>
          </div>
        </div>
      </header>

      <div>
        {messages.length ? (
          messages.map((message) => (
            <article key={message.id}>
              {conversation.type === "GROUP" &&
              message.senderId !== currentUserId ? (
                <div>
                  {conversation.members.find(
                    (member) => member.userId === message.senderId,
                  )?.displayName ?? "Unknown"}
                </div>
              ) : null}
              <p>{message.content}</p>
              <time>
                {formatShortTime(message.createdAt)}
                {message.senderId === currentUserId ? (
                  <span>
                    {receiptGlyph(
                      receiptStateForMessage(message, currentUserId),
                    )}
                  </span>
                ) : null}
              </time>
            </article>
          ))
        ) : (
          <div>
            <Users size={32} />
            <h3>Start the conversation</h3>
            <p className="muted">
              Messages, typing indicators, and group changes will update here.
            </p>
          </div>
        )}
      </div>
    </section>
  );
}
