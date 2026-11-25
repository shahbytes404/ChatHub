import { Search, UserPlus, X } from "lucide-react";
import type {
  ContactSelection,
  PickerMode,
  UserSummary,
} from "../../core/models";

import styles from "./ContactPicker.module.css";
import { useEffect, useState } from "react";
import { MEMBER_ROLES, type MemberRole } from "../../core/constants";
import { chatApi } from "../../core/chat-api";

interface ContactPickerProps {
  mode: PickerMode;
  canAssignOwner: boolean;
  excludedUserIds: string[];
  onClose: () => void;
  onSelectUser: (selection: ContactSelection) => void;
}

export function ContactPicker({
  mode,
  canAssignOwner,
  excludedUserIds,
  onClose,
  onSelectUser,
}: ContactPickerProps) {
  const [query, setQuery] = useState("");
  const [role, setRole] = useState<MemberRole>(MEMBER_ROLES.MEMBER);
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let cancelled = false;

    const loadUsers = async () => {
      setLoading(true);
      try {
        const results = await chatApi.users(query);
        if (!cancelled) {
          const excluded = new Set(excludedUserIds);
          setUsers(results.filter((user) => !excluded.has(user.id)));
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    // Start the async search and discard the promise so the effect can return its cleanup.
    void loadUsers();
    // Before the effect reruns or the picker unmounts, mark this run stale.
    // If its request finishes later, it skips updating the results and loading state.
    return () => {
      cancelled = true;
    };
  }, [excludedUserIds, query]);

  return (
    <div className={styles.modalBackdrop} onClick={onClose}>
      <section
        className={styles.modalSheet}
        onClick={(event) => event.stopPropagation()}>
        <header className={styles.modalHeader}>
          <div>
            <h2 className={styles.modalTitle}>
              {mode === "direct" ? "New chat" : "Add people"}
            </h2>
            <p className="muted">
              {mode === "direct"
                ? "Pick someone to start a direct conversation."
                : "Adding a person turns a direct chat into a group."}
            </p>
          </div>
          <button
            className={styles.modalClose}
            type="button"
            title="Close"
            onClick={onClose}>
            <X size={20} />
          </button>
        </header>

        <label className={styles.searchField}>
          <Search size={18} />
          <input
            className={styles.searchInput}
            value={query}
            placeholder="Search people"
            onChange={(event) => setQuery(event.target.value)}
          />
        </label>

        {mode === "add" ? (
          <label className={styles.roleField}>
            <span>Role</span>
            <select
              className={styles.roleSelect}
              value={role}
              onChange={(event) => setRole(event.target.value as MemberRole)}>
              <option value={MEMBER_ROLES.MEMBER}>Member</option>
              <option value={MEMBER_ROLES.ADMIN}>Admin</option>
              {canAssignOwner ? (
                <option value={MEMBER_ROLES.OWNER}>Owner</option>
              ) : null}
            </select>
          </label>
        ) : null}

        <div className={styles.contactList}>
          {users.length ? (
            users.map((user) => (
              <button
                key={user.id}
                className={styles.contactRow}
                type="button"
                onClick={() =>
                  onSelectUser(mode === "add" ? { user, role } : { user })
                }>
                <span className="avatar">
                  {user.displayName.slice(0, 1).toUpperCase()}
                </span>
                <span className={styles.contactCopy}>
                  <strong className={styles.contactName}>
                    {user.displayName}
                  </strong>
                  <small className={`${styles.contactEmail} muted`}>
                    {user.email}
                  </small>
                </span>
                <UserPlus size={18} />
              </button>
            ))
          ) : (
            <p className="empty-state">
              {loading ? "Searching..." : "No people found."}
            </p>
          )}
        </div>
      </section>
    </div>
  );
}
