import type {
  ConversationType,
  MemberRole,
  MessageType,
  RealtimeEventType,
  ReceiptState,
} from "./constants";

export type ThemeMode = "light" | "dark";

export interface AuthResponse {
  accessToken: string;
  tokenType: "Bearer";
  expiresAt: string;
  userId: string;
  displayName: string;
  email: string;
}

export type SidebarMode = "chats" | "search";

export interface Member {
  userId: string;
  displayName: string;
  role: MemberRole;
  lastReadSequence: number;
  hiddenAfterSequence?: number | null;
}

export interface Conversation {
  id: string;
  type: ConversationType;
  title: string | null;
  createdBy: string;
  createdAt: string;
  members: Member[];
  latestMessagePreview?: string | null;
}

export interface RealtimeEvent {
  type: RealtimeEventType | string;
  targetUserId: string;
  conversationId: string;
  actorUserId: string;
  messageID?: string;
  payload?: unknown;
  occurredAt: string;
}

export interface DeleteConversationAction {
  conversation: Conversation;
  permanent: boolean;
}

export interface ChatMessage {
  id: string;
  conversationId: string;
  senderId: string;
  clientMessageId: string;
  sequenceNumber: number;
  type: MessageType;
  content: string;
  createdAt: string;
  receiptState?: ReceiptState | null;
}

export interface MessagePage {
  messages: ChatMessage[];
  hasMore: boolean;
  nextAfterSequence: number | null;
}

export interface UserSummary {
  id: string;
  displayName: string;
  email: string;
}

export type PickerMode = "direct" | "add";

export interface ContactSelection {
  user: UserSummary;
  role?: MemberRole;
}
