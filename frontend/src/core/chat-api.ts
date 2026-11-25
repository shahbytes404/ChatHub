import { authStore } from "./auth-store";
import { appConfig } from "./config";
import type { ConversationType, MemberRole } from "./constants";
import type {
  AuthResponse,
  ChatMessage,
  Conversation,
  MessagePage,
  UserSummary,
} from "./models";

type RequestOptions = RequestInit & {
  query?: Record<string, string | number | boolean | undefined>;
};

class ChatApi {
  private async request<T>(
    path: string,
    options: RequestOptions = {},
  ): Promise<T> {
    const url = new URL(`${appConfig.apiBase}${path}`);

    Object.entries(options.query ?? {}).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        url.searchParams.set(key, String(value));
      }
    });

    const headers = new Headers(options.headers);
    headers.set("Accept", "application/json");

    const token = authStore.getSnapshot()?.accessToken;
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }

    if (options.body && !headers.has("Content-Type")) {
      headers.set("Content-type", "application/json");
    }

    const response = await fetch(url, {
      ...options,
      headers,
    });

    if (!response.ok) {
      throw await this.readError(response);
    }

    if (response.status === 204) {
      return undefined as T;
    }

    return (await response.json()) as T;
  }

  private async readError(response: Response) {
    try {
      const payload = await response.json();
      return {
        status: response.status,
        error: payload,
        message: payload?.message,
      };
    } catch {
      return {
        status: response.status,
        error: null,
        message: response.statusText,
      };
    }
  }

  register(payload: { displayName: string; email: string; password: string }) {
    return this.request<AuthResponse>("/auth/register", {
      method: "POST",
      body: JSON.stringify(payload),
    });
  }

  login(payload: { email: string; password: string }) {
    return this.request<AuthResponse>("/auth/login", {
      method: "POST",
      body: JSON.stringify(payload),
    });
  }

  conversations() {
    return this.request<Conversation[]>("/conversations");
  }

  messages(conversationId: string, afterSequence = 0, size = 80) {
    return this.request<MessagePage>(
      `/conversations/${conversationId}/messages`,
      {
        query: { afterSequence, size },
      },
    );
  }

  sendMessage(conversationId: string, content: string) {
    return this.request<ChatMessage>(
      `/conversations/${conversationId}/messages`,
      {
        method: "POST",
        body: JSON.stringify({ content, clientMessageId: crypto.randomUUID() }),
      },
    );
  }

  deleteConversation(conversationId: string, permanent = false) {
    return this.request<void>(`/conversations/${conversationId}`, {
      method: "DELETE",
      query: { permanent },
    });
  }

  createConversation(payload: {
    type: ConversationType;
    title?: string | null;
    memberIds: string[];
  }) {
    return this.request<Conversation>("/conversations", {
      method: "POST",
      body: JSON.stringify(payload),
    });
  }

  addMember(
    conversationId: string,
    userId: string,
    role: MemberRole = "MEMBER",
  ) {
    return this.request<Conversation>(
      `/conversations/${conversationId}/members`,
      {
        method: "POST",
        body: JSON.stringify({ userId, role }),
      },
    );
  }

  users(query = "") {
    return this.request<UserSummary[]>("/users", { query: { q: query } });
  }
}

export const chatApi = new ChatApi();
