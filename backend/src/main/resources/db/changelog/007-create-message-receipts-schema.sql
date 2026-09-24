create table message_receipts
(
    id           uuid primary key,
    message_id   uuid not null references messages (id) on delete cascade,
    user_id      uuid not null references users (id) on delete cascade,
    delivered_at timestamp,
    read_at      timestamp,
    unique (message_id, user_id)
);

create index idx_receipts_user on message_receipts (user_id, delivered_at, read_at)