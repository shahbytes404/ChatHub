import { useState } from "react";
import type { Conversation } from "../core/models";

interface useConversationOptions {
  currentUserId: string | undefined;
  onError: (message: string) => void;
}

export function useConversations({
  currentUserId,
  onError,
}: useConversationOptions) {
  const [conversations, setConversations] = useState<Conversation[]>([]);
}
