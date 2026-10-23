import { useSyncExternalStore } from "react";
import { STORAGE_KEYS } from "./constants";
import type { AuthResponse } from "./models";

type Listener = () => void;

class AuthStore {
  private session: AuthResponse | null = this.load();

  private listeners = new Set<Listener>();

  subscribe = (listener: Listener) => {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  };

  getSnapshot = () => this.session;

  signIn(session: AuthResponse) {
    this.session = session;
    localStorage.setItem(STORAGE_KEYS.AUTH, JSON.stringify(session));

    this.emit();
  }

  signOut() {
    this.session = null;
    localStorage.removeItem(STORAGE_KEYS.AUTH);

    this.emit();
  }

  private emit() {
    this.listeners.forEach((listener) => listener());
  }

  private load(): AuthResponse | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.AUTH);
      if (!raw) {
        return null;
      }
      const parsed = JSON.parse(raw) as AuthResponse;
      if (new Date(parsed.expiresAt).getTime() <= Date.now()) {
        localStorage.removeItem(STORAGE_KEYS.AUTH);
        return null;
      }
      return parsed;
    } catch {
      localStorage.removeItem(STORAGE_KEYS.AUTH);
      return null;
    }
  }
}

export const authStore = new AuthStore();

export const useAuthSession = () =>
  useSyncExternalStore(
    authStore.subscribe,
    authStore.getSnapshot,
    authStore.getSnapshot,
  );
