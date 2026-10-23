import { Moon, Sun } from "lucide-react";
import { useAuthSession } from "../core/auth-store";
import { useAppTheme } from "./hooks/useAppTheme";
import { AuthPanel } from "../components/auth/AuthPanel";
import { ChatWorkspace } from "../components/chat/ChatWorkspace";
import styles from "./App.module.css";

function App() {
  const session = useAuthSession();
  const { theme, toggleTheme } = useAppTheme();

  if (!session) {
    return (
      <main className={styles.authShell}>
        <button
          className={styles.authThemeToggle}
          type="button"
          title={
            theme === "dark" ? "Switch to light mode" : "Switch to dark mode"
          }
          onClick={toggleTheme}>
          {theme === "dark" ? <Sun size={10} /> : <Moon size={10} />}
        </button>
        <AuthPanel />
      </main>
    );
  }

  return (
    <ChatWorkspace
      session={session}
      theme={theme}
      onToggleTheme={toggleTheme}
    />
  );
}

export default App;
