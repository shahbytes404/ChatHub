import { useEffect, useRef, type Dispatch, type SetStateAction } from "react";
import type { Conversation, RealtimeEvent } from "../core/models";
import { RealtimeClient } from "../core/realtime-client";
import { REALTIME_EVENTS } from "../core/constants";

interface UseChatRealtimeOptions {
  accessToken: string | undefined;

  /**
   * setConnected is a function that updates a React state variable whose value is a boolean.
   * React defines a type called Dispatch.
   * Conceptually, it represents a function that accepts an argument:
   * type Dispatch<T> = (value: T) => void;
   *
   * React defines SetStateAction conceptually like this:
   * type SetStateAction<S> = S | ((prevState: S) => S);
   * 
   * Option A: Pass the new value setConnected(true);
    
  Option B: Pass a function that calculates the new value
    setConnected(previous => !previous);
   *
   *
   */
  setConnected: Dispatch<SetStateAction<boolean>>;
}

export function useChatRealtime({
  accessToken,
  setConnected,
}: UseChatRealtimeOptions) {
  // we can pass it to caller to update its state, like typng or others
  const realtimeRef = useRef<RealtimeClient | null>(null);

  // 1. Handle newly created messages
  //   async function handleMessageCreated(event: RealtimeEvent) {
  //     if (!event.payload) return;

  //     const message = event.payload as ChatMessage;

  //     let conversation = conversations.find(
  //       (item) => item.id === message.conversationId,
  //     );

  //     // Conversation might not exist in local state yet.
  //     if (!conversation) {
  //       const loaded = await loadConversations(true);

  //       conversation = loaded?.find((item) => item.id === message.conversationId);

  //       if (!conversation) return;

  //       void loadPresence(conversation.id);
  //     }

  //     const added = appendMessage(message, conversation);
  //     const isIncoming = message.senderId !== currentUserId;

  //     // Acknowledge delivery of incoming text messages.
  //     if (added && message.type === "TEXT" && isIncoming) {
  //       void acknowledgeReceipt(
  //         message.conversationId,
  //         message.id,
  //         RECEIPT_TYPES.DELIVERED,
  //       );
  //     }

  //     // Increment unread count for messages in other conversations.
  //     if (
  //       added &&
  //       isIncoming &&
  //       message.conversationId !== selectedConversationId
  //     ) {
  //       setUnreadByConversation((state) => ({
  //         ...state,
  //         [message.conversationId]: (state[message.conversationId] ?? 0) + 1,
  //       }));
  //     }
  //   }

  //   // 2. Handle message delivery and read receipts
  //   function handleMessageReceipt(event: RealtimeEvent) {
  //     if (!event.payload) return;

  //     applyReceiptEvent(event.payload as ReceiptResponse);
  //   }

  //   // 3. Handle conversation creation
  //   function handleConversationCreated(event: RealtimeEvent) {
  //     if (!event.payload) return;

  //     const conversation = event.payload as Conversation;

  //     if (conversation.createdBy === currentUserId) {
  //       upsertConversation(conversation);

  //       if (!selectedConversationId) {
  //         void selectConversation(conversation);
  //       }
  //     } else {
  //       void loadConversations(true);
  //     }
  //   }

  //   // 4. Handle conversation updates
  //   function handleConversationUpdated(event: RealtimeEvent) {
  //     if (!event.payload) return;

  //     const conversation = event.payload as Conversation;

  //     upsertConversation(conversation);

  //     if (!selectedConversationId) {
  //       void selectConversation(conversation);
  //     }

  //     if (conversation.id === selectedConversationId) {
  //       void loadPresence(conversation.id);
  //     }
  //   }

  //   // 5. Handle conversation removal
  //   function handleConversationRemoved(event: RealtimeEvent) {
  //     const payload = event.payload as ConversationRemovedPayload;

  //     removeConversation(payload?.conversationId || event.conversationId);
  //   }

  //   // 6. Handle typing indicators
  //   function handleTypingRealtimeEvent(event: RealtimeEvent) {
  //     handleTypingEvent(
  //       event.conversationId,
  //       event.actorUserId,
  //       event.type === REALTIME_EVENTS.TYPING_STARTED,
  //       (event.payload ?? {}) as TypingPayload,
  //     );
  //   }

  // Main dispatcher
  const handleRealtimeEvent = async (event: RealtimeEvent) => {
    switch (event.type) {
      case REALTIME_EVENTS.MESSAGE_CREATED:
        // await handleMessageCreated(event);
        break;

      case REALTIME_EVENTS.MESSAGE_DELIVERED:
      case REALTIME_EVENTS.MESSAGE_READ:
        // handleMessageReceipt(event);
        break;

      case REALTIME_EVENTS.CONVERSATION_CREATED:
        // handleConversationCreated(event);
        break;

      case REALTIME_EVENTS.CONVERSATION_UPDATED:
        // handleConversationUpdated(event);
        break;

      case REALTIME_EVENTS.CONVERSATION_REMOVED:
        // handleConversationRemoved(event);
        break;

      case REALTIME_EVENTS.TYPING_STARTED:
      case REALTIME_EVENTS.TYPING_STOPPED:
        // handleTypingRealtimeEvent(event);
        break;
    }
  };

  useEffect(() => {
    if (!accessToken) {
      return;
    }

    const realtime = new RealtimeClient();

    realtimeRef.current = realtime;

    realtime.connect(accessToken, handleRealtimeEvent, setConnected);

    return () => {
      realtime.disconnect();
      realtimeRef.current = null;
    };
  }, [accessToken]);

  return {
    realtimeRef,
  };
}
