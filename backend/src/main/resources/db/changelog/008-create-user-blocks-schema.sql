create table user_blocks
(
    id         uuid primary key,
    blocker_id uuid      not null references users (id) on delete cascade,
    blocked_id uuid      not null references users (id) on delete cascade,
    created_at timestamp not null,
    unique (blocked_id, blocker_id),
    check (blocked_id <> blocker_id)
)