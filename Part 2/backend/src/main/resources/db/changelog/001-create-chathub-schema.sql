CREATE TABLE users
(
    id            UUID PRIMARY KEY,
    display_name  VARCHAR(80)  NOT NULL,
    email         VARCHAR(254) not null unique,
    password_hash varchar(100) not null,
    enabled       boolean      not null default true,
    created_at    timestamp    not null,
    updated_at    timestamp    not null
);

create table conversations
(
    id                    uuid primary key,
    type                  varchar(20) not null,
    title                 varchar(120),
    created_by            uuid        not null references users (id),
    next_message_sequence bigint      not null DEFAULT 1,
    created_at            timestamp   not null,
    updated_at            timestamp   not null
);

create table conversation_members
(
    id                 uuid primary key,
    conversation_id    uuid        not null references conversations (id) on DELETE CASCADE,
    user_id            uuid        not null references users (id) on delete cascade,
    role               varchar(20) not null,
    last_read_sequence bigint      not null default 0,
    muted_until        timestamp,
    joined_at          timestamp   not null,

    UNIQUE (conversation_id, user_id)
);

create index idx_members_user on conversation_members (user_id);

create table messages
(
    id                uuid primary key,
    conversation_id   uuid          not null references conversations (id) on delete cascade,
    sender_id         uuid          not null references users (id),
    client_message_id varchar(100)  not null,
    sequence_number   bigint        not null,
    message_type      bigint        not null,
    content           varchar(4000) not null,
    created_at        timestamp     not null,
    edited_at         timestamp,
    deleted_at        timestamp,
    UNIQUE (sender_id, client_message_id),
    unique (conversation_id, sequence_number)
);

-- missed messages = where conversation_id = ? and sequence_number > ?
-- where conversation_id = ? order by sequence_number DESC
create index idx_messages_conversation_sequence on messages (conversation_id, sequence_number DESC);

create table message_receipts
(
    id           uuid primary key,
    message_id   uuid not null references messages (id) on delete cascade,
    user_id      uuid not null references users (id) on delete cascade,
    delivered_at timestamp,
    read_at      timestamp,
    unique (message_id, user_id)
);

create index idx_receipts_user on message_receipts (user_id, delivered_at, read_at);

create table device_registration
(
    id                    uuid primary key,
    user_id               uuid         not null references users (id) on delete cascade,
    device_id             varchar(100) not null,
    platform              varchar(20)  not null,
    push_token            varchar(500),
    notifications_enabled boolean      not null default true,
    last_seen_at          timestamp    not null,
    created_at            timestamp    not null,
    unique (user_id, device_id)
);

create table user_blocks
(
    id         uuid primary key,
    blocker_id uuid      not null references users (id) on delete cascade,
    blocked_id uuid      not null references users (id) on delete cascade,
    created_at timestamp not null,
    unique (blocker_id, blocked_id),
    check ( blocker_id <> blocked_id )
);

create table outbox_events
(
    id             uuid primary key,
    aggregate_type varchar(40) not null,
    aggregate_id   uuid        not null,
    event_type     varchar(60) not null,
    payload_json   text        not null,
    -- worker state
    -- READY, PUBLISHED
    status         varchar(20) not null,
    attempts       integer     not null default 0,
    available_at   timestamp   not null,
    created_at     timestamp   not null,
    published_at   timestamp
);

-- where status = 'READY' and available_at <= now() order By created_at
create index idx_outbox_ready on outbox_events (status, available_at, created_at);

create table processed_events
(
    event_id     uuid primary key,
    processed_at timestamp not null
);

create table audit_events
(
    id            uuid primary key,
    actor_id      uuid,
    action        varchar(80) not null,
    resource_type varchar(40) not null,
    resource_id   varchar(100),

    metadata_json text        not null,
    occurred_at   timestamp   not null
);

create index idx_audit_actor_time on audit_events (actor_id, occurred_at DESC);