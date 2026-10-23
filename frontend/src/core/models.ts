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
