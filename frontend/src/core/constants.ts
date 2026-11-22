export const STORAGE_KEYS = {
  AUTH: "chathub.auth",
  THEME: "chathub.theme",
} as const;

export const CONVERSATION_TYPES = {
  DIRECT: "DIRECT",
  GROUP: "GROUP",
} as const;

export type ConversationType =
  (typeof CONVERSATION_TYPES)[keyof typeof CONVERSATION_TYPES];

export const MEMBER_ROLES = {
  OWNER: "OWNER",
  ADMIN: "ADMIN",
  MEMBER: "MEMBER",
} as const;

export type MemberRole = (typeof MEMBER_ROLES)[keyof typeof MEMBER_ROLES];
