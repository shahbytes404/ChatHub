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

export const REALTIME_EVENTS = {
  MESSAGE_CREATED: "MESSAGE_CREATED",
  MESSAGE_DELIVERED: "MESSAGE_DELIVERED",
  MESSAGE_READ: "MESSAGE_READ",

  CONVERSATION_CREATED: "CONVERSATION_CREATED",
  CONVERSATION_UPDATED: "CONVERSATION_UPDATED",
  CONVERSATION_REMOVED: "CONVERSATION_REMOVED",

  TYPING_STARTED: "TYPING_STARTED",
  TYPING_STOPPED: "TYPING_STOPPED",
} as const;

export type RealtimeEventType =
  (typeof REALTIME_EVENTS)[keyof typeof REALTIME_EVENTS];

export const RECEIPT_STATES = {
  SENT: "SENT",
  DELIVERED: "DELIVERED",
  READ: "READ",
} as const;

export type ReceiptState = (typeof RECEIPT_STATES)[keyof typeof RECEIPT_STATES];

export const RECEIPT_TYPES = {
  DELIVERED: "DELIVERED",
  READ: "READ",
} as const;

export type ReceiptType = (typeof RECEIPT_TYPES)[keyof typeof RECEIPT_TYPES];

export const MESSAGE_TYPES = {
  TEXT: "TEXT",
  SYSTEM: "SYSTEM",
} as const;

export type MessageType = (typeof MESSAGE_TYPES)[keyof typeof MESSAGE_TYPES];
