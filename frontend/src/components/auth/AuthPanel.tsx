import { useMemo, useState } from "react";
import type { SubmitEvent } from "react";
import { chatApi } from "../../core/chat-api";
import { authStore } from "../../core/auth-store";
import { LogIn, MessageCircle, UserPlus } from "lucide-react";
import styles from "./AuthPanel.module.css";

type AuthMode = "login" | "register";

type FieldErrors = {
  displayName?: string;
  email?: string;
  password?: string;
};

// test@google.com
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function AuthPanel() {
  const [mode, setMode] = useState<AuthMode>("login");
  const [displayName, setDisplayName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const fieldErrors = useMemo<FieldErrors>(() => {
    const next: FieldErrors = {};

    if (mode === "register" && !displayName.trim()) {
      next.displayName = "Display name is required.";
    } else if (displayName.length > 80) {
      next.displayName = "Display name is too long.";
    }

    if (!email.trim()) {
      next.email = "Email is required.";
    } else if (!EMAIL_REGEX.test(email)) {
      next.email = "Enter a valid email address.";
    }

    if (!password) {
      next.password = "Password is required.";
    } else if (password.length < 10) {
      next.password = "Password must be at least 10 characters.";
    }

    return next;
  }, [displayName, email, mode, password]);

  const canSubmit = !loading && Object.keys(fieldErrors).length === 0;

  const submit = async (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!canSubmit) {
      return;
    }

    setLoading(true);
    setError("");

    try {
      const session =
        mode === "login"
          ? await chatApi.login({ email: email.trim(), password })
          : await chatApi.register({
              displayName: displayName.trim(),
              email: email.trim(),
              password,
            });

      authStore.signIn(session);
    } catch (requestError) {
      const message =
        requestError instanceof Error
          ? requestError.message
          : (
              requestError as {
                error?: { message?: string };
                message?: string;
              }
            )?.error?.message ||
            (requestError as { message?: string })?.message ||
            "Could not sign in with those details";
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className={styles.authPage}>
      <section className={styles.authCard}>
        <div className={styles.brandRow}>
          <div className={styles.brandMark}>
            <MessageCircle size={28} />
          </div>
          <h1 className={styles.brandTitle}>ChatHub</h1>
        </div>
        <p className={`muted ${styles.brandCopy}`}>
          Fast, secure chat for teams and firends.
        </p>

        <div
          className={styles.segmented}
          role="tablist"
          aria-label="Authentication mode">
          <button
            type="button"
            className={`${styles.segmentedOption} ${mode === "login" ? styles.segmentedOptionActive : ""}`}
            aria-selected={mode === "login"}
            onClick={() => setMode("login")}>
            Login
          </button>
          <button
            type="button"
            className={`${styles.segmentedOption} ${mode === "register" ? styles.segmentedOptionActive : ""}`}
            aria-selected={mode === "register"}
            onClick={() => setMode("register")}>
            Register
          </button>
        </div>

        <form className={styles.form} onSubmit={submit}>
          {mode === "register" ? (
            <label className={styles.field}>
              <span>Display name</span>
              <input
                className={styles.input}
                autoComplete="name"
                maxLength={80}
                value={displayName}
                onChange={(event) => setDisplayName(event.target.value)}
              />
              {fieldErrors.displayName ? (
                <small className={styles.fieldError}>
                  {fieldErrors.displayName}
                </small>
              ) : null}
            </label>
          ) : null}

          <label className={styles.field}>
            <span>Email</span>
            <input
              className={styles.input}
              type="email"
              autoComplete="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
            />
            {fieldErrors.email ? (
              <small className={styles.fieldError}>{fieldErrors.email}</small>
            ) : null}
          </label>

          <label className={styles.field}>
            <span>Password</span>
            <input
              className={styles.input}
              type="password"
              autoComplete={
                mode === "login" ? "current-password" : "new-password"
              }
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
            {fieldErrors.password ? (
              <small className={styles.fieldError}>
                {fieldErrors.password}
              </small>
            ) : null}
          </label>

          {error ? <p className={styles.formError}>{error}</p> : null}

          <button
            className={styles.primaryAction}
            type="submit"
            disabled={!canSubmit}>
            {mode === "login" ? <LogIn size={18} /> : <UserPlus size={18} />}
            <span>{mode === "login" ? "Sign in" : "Create account"}</span>
          </button>
        </form>
      </section>
    </main>
  );
}
