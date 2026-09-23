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
    conversation_id    uuid        not null references conversations (id) on delete cascade,
    user_id            uuid        not null references users (id),
    role               varchar(20) not null,
    last_read_sequence bigint      not null default 0,
    joined_at          timestamp   not null,

    UNIQUE (conversation_id, user_id)
);