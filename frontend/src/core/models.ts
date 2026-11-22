import type { ConversationType, MemberRole } from "./constants";

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
